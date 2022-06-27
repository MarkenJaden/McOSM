package bleach.mcosm.command;

import bleach.mcosm.McOSM;
import bleach.mcosm.gui.GuiOSM;
import bleach.mcosm.utils.GeoPos;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SetOSMCommand extends CommandBase {

	private BlockPos pos1;
	private BlockPos pos2;

	@Override
	public String getName() {
		return "setosm";
	}
 
	@Override
	public String getUsage(ICommandSender sender) {
		return "/setosm <1/2> | /setosm start | /setosm clear";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length > 1) throw new WrongUsageException(getUsage(sender), new Object[0]);

		if (args.length == 0) start();

		switch (args[0].toLowerCase()) {
			case "clear":
				pos1 = null;
				pos2 = null;
				break;
			case "1":
				pos1 = sender.getPosition();
				break;
			case "2":
				pos2 = sender.getPosition();
				break;
			case "start":
				start();
				break;
		}
	}

	private void start(){
		double[] coords1 = GeoPos.toLatLonBTE(pos1);
		double[] coords2 = GeoPos.toLatLonBTE(pos2);

		McOSM.guiQueue.add(new GuiOSM(coords1[0], coords1[1], coords2[0], coords2[1], pos1.getY()));
	}

}
