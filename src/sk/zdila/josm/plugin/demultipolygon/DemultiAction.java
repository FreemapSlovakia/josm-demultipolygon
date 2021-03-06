// License: GPL. Copyright 2007 by Immanuel Scholz and others
package sk.zdila.josm.plugin.demultipolygon;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.RelationToChildReference;
import org.openstreetmap.josm.tools.Shortcut;

public class DemultiAction extends JosmAction {

    private static final long serialVersionUID = 6854238214548011750L;

    public DemultiAction() {
        super(tr("Demultipolygon"), null, tr("demultipolygon"),
                Shortcut.registerShortcut("tools:demultipolygon", tr("Tool: {0}", tr("Demultipolygon")), KeyEvent.VK_M, Shortcut.SHIFT), true);
    }



    @Override
    public void actionPerformed(final ActionEvent e) {
        final Collection<OsmPrimitive> selection = getCurrentDataSet().getSelected();
        final Set<RelationToChildReference> relationToChildReferences = RelationToChildReference.getRelationToChildReferences(selection);

        final Collection<OsmPrimitive> toDelete = new LinkedHashSet<OsmPrimitive>();

        final Map<OsmPrimitive, Map<String, String>> map = new HashMap<OsmPrimitive, Map<String, String>>();

        for (final RelationToChildReference r : relationToChildReferences) {
			toDelete.add(r.getParent());

        	if (r.getRole().equals("inner")) {
        		for (final RelationMember rm : r.getParent().getMembers()) {
        			if (rm.getRole().equals("outer")) {
        				map.put(r.getChild(), new HashMap<String, String>(rm.getMember().getKeys()));
//        				for (final Entry<String, String> entry : rm.getMember().getKeys().entrySet()) {
//        					commands2.add(new ChangePropertyCommand(Collections.singleton(r.getChild()), entry.getKey(), entry.getValue()));
//        				}

        				toDelete.add(rm.getMember());
        			}
        		}
        	} else if (r.getRole().equals("outer")) {
    			toDelete.add(r.getChild());

        		for (final RelationMember rm : r.getParent().getMembers()) {
        			if (rm.getRole().equals("inner")) {
        				map.put(rm.getMember(), r.getChild().getKeys());
//        				for (final Entry<String, String> entry : r.getChild().getKeys().entrySet()) {
//        					commands2.add(new ChangePropertyCommand(Collections.singleton(rm.getMember()), entry.getKey(), entry.getValue()));
//        				}
        			}
        		}
        	}
        }

        final Collection<Command> commands = new ArrayList<Command>();
        commands.add(DeleteCommand.delete(Main.main.getEditLayer(), toDelete, true, true));

        for (final Entry<OsmPrimitive, Map<String, String>> entry : map.entrySet()) {
            for (final Entry<String, String> entry2 : entry.getValue().entrySet()) {
            	commands.add(new ChangePropertyCommand(Collections.singleton(entry.getKey()), entry2.getKey(), entry2.getValue()));
            }
        }

        Main.main.undoRedo.add(new SequenceCommand("demultipolygon", commands));
        Main.map.repaint();
    }


    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }


    @Override
    protected void updateEnabledState(final Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }

}
