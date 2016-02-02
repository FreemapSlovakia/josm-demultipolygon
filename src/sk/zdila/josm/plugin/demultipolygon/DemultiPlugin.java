package sk.zdila.josm.plugin.demultipolygon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class DemultiPlugin extends Plugin {

    public DemultiPlugin(final PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.toolsMenu, new DemultiAction());
    }

}
