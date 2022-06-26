package bleach.mcosm.api;

import bleach.mcosm.OSMInstance;
import bleach.mcosm.network.StartOSMProcessHandler;
import bleach.mcosm.struct.building.BuildingStruct;
import bleach.mcosm.struct.natural.TreeRowStruct;
import bleach.mcosm.struct.natural.TreeStruct;
import bleach.mcosm.struct.road.RoadStruct;
import bleach.mcosm.utils.BlockColors;
import bleach.mcosm.utils.GeoPos;
import bleach.mcosm.utils.gen.TreeType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockConcretePowder;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class ApiDataHandler {

    private JsonObject data;
    public Projection proj;

    public List<JsonObject> ways = new ArrayList<>();
    public List<JsonObject> nodes = new ArrayList<>();

    public double minLat = Integer.MAX_VALUE;
    public double minLon = Integer.MAX_VALUE;
    public double maxLat = Integer.MIN_VALUE;
    public double maxLon = Integer.MIN_VALUE;

    public ApiDataHandler(JsonObject json, Projection proj) {
        this.data = json;
        this.proj = proj;

        PriorityQueue<OrderedData> tempWays = new PriorityQueue<>();

        for (JsonElement j : data.get("elements").getAsJsonArray()) {
            JsonObject jobj = j.getAsJsonObject();

            try {
                JsonObject jbounds = jobj.get("bounds").getAsJsonObject();
                minLat = Math.min(minLat, jbounds.get("minlat").getAsDouble());
                minLon = Math.min(minLon, jbounds.get("minlon").getAsDouble());
                maxLat = Math.max(maxLat, jbounds.get("maxlat").getAsDouble());
                maxLon = Math.max(maxLon, jbounds.get("maxlon").getAsDouble());
            } catch (Exception ignored) {
            }

            switch (jobj.get("type").getAsString()) {
                case "node":
                    nodes.add(jobj);
                    break;
                case "way":
                    JsonElement jtags = jobj.get("tags");
                    if (jtags != null) {
                        if (jtags.getAsJsonObject().has("building")) {

                            tempWays.add(new OrderedData(4, jobj));

                        } else if (jtags.getAsJsonObject().has("highway")) {

                            if (jtags.getAsJsonObject().get("highway").getAsString().equals("service")) {
                                tempWays.add(new OrderedData(3, jobj));
                            } else if (jtags.getAsJsonObject().get("highway").getAsString().equals("cycleway")) {
                                tempWays.add(new OrderedData(2, jobj));
                            } else if (jtags.getAsJsonObject().get("highway").getAsString().equals("footway")
                                    || jtags.getAsJsonObject().get("highway").getAsString().equals("steps")) {
                                tempWays.add(new OrderedData(1, jobj));
                            } else {
                                tempWays.add(new OrderedData(0, jobj));
                            }

                        } else if (jtags.getAsJsonObject().has("natural") || jtags.getAsJsonObject().has("barrier")) {
                            tempWays.add(new OrderedData(5, jobj));
                        }
                    }

                    break;
            }
        }

        while (!tempWays.isEmpty())
            ways.add(tempWays.poll().data);
    }

    public void addToInstance(OSMInstance inst) {
        for (JsonObject j : nodes) {
            double lat = j.get("lat").getAsDouble();
            double lon = j.get("lon").getAsDouble();

            if (j.get("tags") != null) {
                JsonObject jtags = j.get("tags").getAsJsonObject();

                if (jtags.get("natural").getAsString().equals("tree")) inst.add(
                        new TreeStruct(latLonToPos(lat, lon, minLat + (maxLat - minLat) / 2, minLon + (maxLon - minLon) / 2)));
            }
        }

        for (JsonObject j : ways) {
            List<BlockPos> nodes = new ArrayList<>();

            JsonElement jgeom = j.get("geometry");
            if (jgeom != null) {
                for (JsonElement jg : jgeom.getAsJsonArray()) {
                    if (jg.isJsonNull()) continue;

                    JsonObject jgobj = jg.getAsJsonObject();

                    nodes.add(latLonToPos(jgobj.get("lat").getAsDouble(), jgobj.get("lon").getAsDouble(),
                            minLat + (maxLat - minLat) / 2, minLon + (maxLon - minLon) / 2));
                }
            }

            if (j.has("tags")) {
                JsonObject jtags = j.get("tags").getAsJsonObject();

                if (jtags.has("building")) {
                    JsonElement jbuilding = jtags.get("building");

                    IBlockState blockType = Blocks.CONCRETE.getDefaultState();
                    IBlockState windowType = Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.SILVER);

                    int height = 7;
                    int floors = 0;

                    JsonElement jfloors = jtags.get("building:levels");
                    JsonElement jheight = jtags.get("height");
                    JsonElement jcolor = jtags.get("building:colour");
                    JsonElement jmaterial = jtags.get("building:material");

                    if (jfloors != null) {
                        height = (int) Math.round(jfloors.getAsInt() * 3.5);
                        floors = jfloors.getAsInt();
                    }

                    if (jheight != null) {
                        height = (int) jheight.getAsDouble();
                    }

                    if (jcolor != null) {
                        String color = jcolor.getAsString().replace("#", "");

                        try {
                            blockType = BlockColors.getClosestBlock(Integer.parseInt(color, 16));
                        } catch (NumberFormatException nfe) {
                            try {
                                Field f = Color.class.getField(color.toUpperCase());

                                blockType = BlockColors.getClosestBlock(((Color) f.get(null)).getRGB());
                            } catch (Exception e) {
                            }
                        }
                    }

                    if (jmaterial != null) {
                        switch (jmaterial.getAsString()) {
                            case "brick":
                                blockType = Blocks.BRICK_BLOCK.getDefaultState();
                        }
                    }

                    if (jbuilding.getAsString().equals("garage")) {
                        blockType = Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY);
                        if (jfloors == null && jheight == null) height = 4;
                    }

                    inst.add(new BuildingStruct(nodes, blockType, floors > 0 ? windowType : null, height, floors));
                }

                if (jtags.has("highway")) {
                    JsonElement jroad = jtags.get("highway");
                    nodes = nodes.stream().map(b -> b.down()).collect(Collectors.toList());

                    switch (jroad.getAsString()) {
                        case "motorway":
                            inst.add(new RoadStruct(nodes,
                                    Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 5,
                                    Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.YELLOW), 5));
                            break;
                        case "primary":
                            inst.add(new RoadStruct(nodes,
                                    Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 4,
                                    Blocks.CONCRETE.getDefaultState(), 4));
                            break;
                        case "secondary":
                            inst.add(new RoadStruct(nodes,
                                    Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 4));
                            break;
                        case "trunk":
                            inst.add(new RoadStruct(nodes,
                                    Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 4,
                                    Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.YELLOW), 4));
                            break;
                        case "tertiary":
                        case "motorway_link":
                            inst.add(new RoadStruct(nodes, Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 3));
                            break;
                        case "service":
                            if (jtags.has("service")) {
                                JsonElement jservice = jtags.get("service");

                                switch (jservice.getAsString()) {
                                    case "alley":
                                    case "drive-through":
                                        inst.add(new RoadStruct(nodes, Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 1));
                                        break;
                                    case "driveway":
                                        inst.add(new RoadStruct(nodes, Blocks.GRAVEL.getDefaultState(), 1));
                                        break;
                                    default:
                                        inst.add(new RoadStruct(nodes, Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 2));
                                }
                            } else {
                                inst.add(new RoadStruct(nodes, Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 2));
                            }

                            break;
                        case "cycleway":
                        case "pedestrian":
                            inst.add(new RoadStruct(nodes,
                                    Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.CYAN), 1));
                            break;
                        case "track":
                            inst.add(new RoadStruct(nodes, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), 1));
                            break;
                        case "residential":
                        case "unclassified":
                        case "trunk_link":
                        case "primary_link":
                        case "secondary_link":
                        case "tertiary_link":
                            inst.add(new RoadStruct(nodes, Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 2));
                            break;
                        case "footway":
                        case "steps":
                            inst.add(new RoadStruct(nodes, Blocks.BRICK_BLOCK.getDefaultState(), 1));
                            break;
                        default:
							/*inst.add(new RoadStruct(nodes,
									Blocks.CONCRETE.getDefaultState().withProperty(BlockConcretePowder.COLOR, EnumDyeColor.GRAY), 4,
									Blocks.CONCRETE.getDefaultState(), 4));*/
                    }
                }

                if (jtags.has("natural")) {
                    JsonElement jnatural = jtags.get("natural");

                    if (jnatural.getAsString().equals("tree_row")) {
                        inst.add(new TreeRowStruct(nodes, TreeType.CANOPY, 3));
                    }
                }

                if (jtags.has("barrier")) {
                    JsonElement jbarrier = jtags.get("barrier");

                    if (jbarrier.getAsString().equals("hedge")) {
                        inst.add(new TreeRowStruct(nodes, TreeType.HEDGE, 0));
                    }
                }
            }
        }
    }

    private BlockPos latLonToPos(double lat, double lon, double lat0, double lon0) {
        switch (proj) {
            case NAIVE_00:
                if (Minecraft.getMinecraft().isSingleplayer())
                    return GeoPos.from00Naive(lat, lon, (int) Minecraft.getMinecraft().player.posY);
                else return GeoPos.from00Naive(lat, lon, StartOSMProcessHandler.pos.getY());
            case NAIVE_PLAYER:
                return GeoPos.fromPlayerNaive(lat, lon, lat0, lon0);
            case BTE_00:
                return GeoPos.from00BTE(lat, lon, StartOSMProcessHandler.pos.getY());
            case BTE_PLAYER:
                return GeoPos.fromPlayerBTE(lat, lon, lat0, lon0);
            default:
                System.err.println("Unknown Projection Found!");
                return BlockPos.ORIGIN;
        }
    }

    public enum Projection {
        NAIVE_00,
        NAIVE_PLAYER,
        BTE_00,
        BTE_PLAYER
    }

    private static class OrderedData implements Comparable<OrderedData> {

        public int priority;
        public JsonObject data;

        public OrderedData(int priority, JsonObject data) {
            this.priority = priority;
            this.data = data;
        }

        @Override
        public int compareTo(OrderedData o) {
            return o.priority - priority;
        }
    }
}
