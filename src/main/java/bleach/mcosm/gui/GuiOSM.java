package bleach.mcosm.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import bleach.mcosm.network.McOSMPacketHandler;
import bleach.mcosm.network.StartOSMProcess;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import bleach.mcosm.McOSM;
import bleach.mcosm.api.API;
import bleach.mcosm.api.ApiDataHandler;
import bleach.mcosm.api.ApiDataHandler.Projection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

public class GuiOSM extends GuiMapBase {
	
	private GuiTextField latField;
	private GuiTextField lonField;
	private GuiTextField lat1Field;
	private GuiTextField lon1Field;
	
	private String outputText = "";
	
	public GuiOSM(double lat, double lon, double lat1, double lon1) {
		super(lat, lon, lat1, lon1);
	}
	
	public void initGui() {
		super.initGui();
		
		addButton(new GuiButton(0, mapX + mapLen - 75, mapY + mapHei + 5, 75, 20, "Start"));
		addButton(new GuiButton(1, mapX, mapY + mapHei + 5, 75, 20, "Cancel"));
		
		addButton(new GuiButton(2, mapX - 65, mapY, 60, 20, "\u00a7aBuildings"));
		addButton(new GuiButton(3, mapX - 65, mapY + 22, 60, 20, "\u00a7aRoads"));
		addButton(new GuiButton(4, mapX - 65, mapY + 44, 60, 20, "\u00a7aTrees"));

		latField = new GuiTextField(256, fontRenderer, mapX + mapLen + 4, mapY + 10, 80, 20);
		lonField = new GuiTextField(257, fontRenderer, mapX + mapLen + 4, mapY + 45, 80, 20);
		lat1Field = new GuiTextField(258, fontRenderer, mapX + mapLen + 4, mapY + 80, 80, 20);
		lon1Field = new GuiTextField(259, fontRenderer, mapX + mapLen + 4, mapY + 115, 80, 20);
		
		latField.setText(lat + "");
		lonField.setText(lon + ""); // who needs toString
		lat1Field.setText(lat1 + "");
		lon1Field.setText(lon1 + "");
    }
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		drawString(fontRenderer, "Lat 1:", mapX + mapLen + 4, mapY, 0x909090);
		drawString(fontRenderer, "Lon 1:", mapX + mapLen + 4, mapY + 35, 0x909090);
		drawString(fontRenderer, "Lat 2:", mapX + mapLen + 4, mapY + 70, 0x909090);
		drawString(fontRenderer, "Lon 2:", mapX + mapLen + 4, mapY + 105, 0x909090);
		drawString(fontRenderer, "Output:", mapX + mapLen + 4, mapY + 145, 0xc0c0c0);
		
		int h = mapY + 155;
		for (String s: outputText.split("\n")) {
			drawString(fontRenderer, s, mapX + mapLen + 4, h, 0xc0c0c0);
			h += 10;
		}
		
		latField.drawTextBox();
		lonField.drawTextBox();
		lat1Field.drawTextBox();
		lon1Field.drawTextBox();
	}
	
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0:
				double minLat = Double.parseDouble(latField.getText());
				double minLon = Double.parseDouble(lonField.getText());
				double maxLat = Double.parseDouble(lat1Field.getText());
				double maxLon = Double.parseDouble(lon1Field.getText());
				McOSMPacketHandler.INSTANCE.sendToServer(new StartOSMProcess(minLat, minLon, maxLat, maxLon, buttonList.get(2).displayString.startsWith("\u00a7a"), buttonList.get(3).displayString.startsWith("\u00a7a"), buttonList.get(4).displayString.startsWith("\u00a7a")));

				Minecraft.getMinecraft().displayGuiScreen(null);
				break;
			case 1:
				Minecraft.getMinecraft().displayGuiScreen(null);
				break;
			case 2: case 3: case 4:
				String s = button.displayString;
				button.displayString = (s.startsWith("\u00a7c") ? "\u00a7a" : "\u00a7c") + s.substring(2);
				break;
		}
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		
		latField.textboxKeyTyped(typedChar, keyCode);
		lonField.textboxKeyTyped(typedChar, keyCode);
		lat1Field.textboxKeyTyped(typedChar, keyCode);
		lon1Field.textboxKeyTyped(typedChar, keyCode);
		
		if (latField.isFocused() && NumberUtils.isCreatable(latField.getText())) lat = NumberUtils.createNumber(latField.getText()).doubleValue();
		if (lonField.isFocused() && NumberUtils.isCreatable(lonField.getText())) lon = NumberUtils.createNumber(lonField.getText()).doubleValue();
		if (lat1Field.isFocused() && NumberUtils.isCreatable(lat1Field.getText())) lat1 = NumberUtils.createNumber(lat1Field.getText()).doubleValue();
		if (lon1Field.isFocused() && NumberUtils.isCreatable(lon1Field.getText())) lon1 = NumberUtils.createNumber(lon1Field.getText()).doubleValue();
    }
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		
		latField.mouseClicked(mouseX, mouseY, mouseButton);
		lonField.mouseClicked(mouseX, mouseY, mouseButton);
		lat1Field.mouseClicked(mouseX, mouseY, mouseButton);
		lon1Field.mouseClicked(mouseX, mouseY, mouseButton);
	}
}
