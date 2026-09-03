package net.vas.rdpcore.command;

import net.minecraft.command.ICommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.vas.rdpcore.api.RDPAPI;
import java.util.Arrays;
import java.util.List;

public class RdpCommand extends CommandBase {

    @Override
    public String getName() {
        return "rdp";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/rdp status|simulate <cycles>|set <level>";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("rdpcore");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString("Usage: " + getUsage(sender)));
            return;
        }
        String sub = args[0];
        if (sub.equalsIgnoreCase("status")) {
            double g = RDPAPI.getGlobalRDPLevel(sender.getEntityWorld());
            sender.sendMessage(new TextComponentString(String.format("Global RDP: %.4f (stage: %s)", g, RDPAPI.getCurrentStage(sender.getEntityWorld()))));
        } else if (sub.equalsIgnoreCase("telemetry") || sub.equalsIgnoreCase("debug")) {
            // show telemetry
            net.vas.rdpcore.util.Telemetry t = null; // unused, static access
            sender.sendMessage(new TextComponentString(String.format("Simulation cycles: %d", net.vas.rdpcore.util.Telemetry.simulationCycles.get())));
            sender.sendMessage(new TextComponentString(String.format("Last duration(ms): %d", net.vas.rdpcore.util.Telemetry.lastSimulationDurationMs.get())));
            sender.sendMessage(new TextComponentString(String.format("Regions processed: %d", net.vas.rdpcore.util.Telemetry.regionalUpdates.get())));
            sender.sendMessage(new TextComponentString(String.format("Pressure calc(ms): %d", net.vas.rdpcore.util.Telemetry.pressureCalcDurationMs.get())));
            sender.sendMessage(new TextComponentString(String.format("Anomaly(ms): %d", net.vas.rdpcore.util.Telemetry.anomalyDurationMs.get())));
            sender.sendMessage(new TextComponentString(String.format("Event(ms): %d", net.vas.rdpcore.util.Telemetry.eventDurationMs.get())));
            sender.sendMessage(new TextComponentString(String.format("Mutation planning(ms): %d", net.vas.rdpcore.util.Telemetry.mutationPlanningDurationMs.get())));
            sender.sendMessage(new TextComponentString(String.format("Mutations processed last: %d", net.vas.rdpcore.util.Telemetry.mutationsProcessedLast.get())));
        } else if (sub.equalsIgnoreCase("simulate")) {
            int cycles = 1;
            if (args.length >= 2) {
                try { cycles = Integer.parseInt(args[1]); } catch (NumberFormatException e) { cycles = 1; }
            }
            for (int i=0;i<cycles;i++) {
                net.vas.rdpcore.RDPSimulationEngine.runSimulationForWorld((net.minecraft.world.WorldServer)sender.getEntityWorld());
            }
            sender.sendMessage(new TextComponentString("Simulated " + cycles + " cycles."));
        } else if (sub.equalsIgnoreCase("set") && args.length >= 2) {
            try {
                double level = Double.parseDouble(args[1]);
                RDPAPI.setGlobalRDPLevel(sender.getEntityWorld(), level);
                sender.sendMessage(new TextComponentString("Global RDP set to " + level));
            } catch (NumberFormatException e) {
                sender.sendMessage(new TextComponentString("Invalid number: " + args[1]));
            }
        } else {
            sender.sendMessage(new TextComponentString("Unknown subcommand"));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        // Only ops (players with permission level 4) can run this command
        return sender.canUseCommand(4, this.getName());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, net.minecraft.util.math.BlockPos targetPos) {
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
