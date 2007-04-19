package org.csstudio.util.swt;

import org.csstudio.platform.model.CentralItemFactory;
import org.csstudio.platform.model.IProcessVariable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

import com.sun.org.apache.bcel.internal.generic.IfInstruction;

/** Maintains a 'history' Combo box.
 *  <p>
 *  Newly entered items are added to the top of the combo list,
 *  dropping last items off the list when reaching a comfigurable maximum
 *  list size.
 *  <p>
 *  @see #newSelection(String)
 *  @author Kay Kasemir
 *  @author Last modifications by Helge Rickens
 */
public abstract class ComboHistoryHelper
{
    private static final String TAG = "values"; //$NON-NLS-1$
    private static final int DEFAULT_MAX = 10;
    private final IDialogSettings settings;
    private final String tag;
    private final ComboViewer combo;
    private final int max;

    /** Attach helper to given combo box, using default list length. */
    public ComboHistoryHelper(IDialogSettings settings, String tag, ComboViewer combo)
    {
        this(settings, tag, combo, DEFAULT_MAX);
    }

    /** Attach helper to given combo box, using max list length.
     *  @param settings Where to persist the combo box list
     *  @param tag      Tag used for persistence
     *  @param combo    The ComboViewer
     *  @param max      Max list length
     */
    public ComboHistoryHelper(IDialogSettings settings, String tag,
                              ComboViewer combo, int max)
    {
        this.settings = settings;
        this.tag = tag;
        this.combo = combo;
        this.max = max;

        // React whenever an existing entry is selected,
        // or a new name is entered.
        // New names are also added to the list.
        combo.getCombo().addSelectionListener(new SelectionListener()
        {
            // Called after <Return> was pressed
            public void widgetDefaultSelected(SelectionEvent e)
            {
                String new_entry = ComboHistoryHelper.this.combo.getCombo().getText();
                addEntry(new_entry);
                newSelection(new_entry);
            }

            // Called after existing entry was picked from list
            public void widgetSelected(SelectionEvent e)
            {   handleNewSelection();    }
        });

    }

    /** Add entry to top of list. */
    private void addEntry(String new_entry)
    {
    	IProcessVariable iPV =CentralItemFactory.createProcessVariable(new_entry);
    	combo.getCombo().remove(iPV.getName());
    	combo.add(iPV);
//        // Avoid duplicates
//        for (int i=0; i<combo.getItemCount(); ++i)
//            if (combo.getItem(i).equals(new_entry))
//                return;
//        // Add at top
//        combo.add(new_entry, 0);
//        // Maybe remove oldest entry
        if (combo.getCombo().getItemCount() > max){
//            combo.getCombo().remove(combo.getCombo().getItemCount() - 1);
        	combo.getCombo().remove(0);
        }
        combo.getCombo().select(combo.getCombo().getItemCount()-1);
    }

    /** Notify about new selection. */
    private void handleNewSelection()
    {
        String name = combo.getCombo().getText();
        newSelection(name);
    }

    /** Invoked whenever a new entry was entered or selected. */
    public abstract void newSelection(String entry);

    /** Load persisted list values. */
    public void loadSettings()
    {
        IDialogSettings pvs = settings.getSection(tag);
        if (pvs == null)
            return;
        String values[] = pvs.getArray(TAG);
        if (values != null)
            for (int i = 0; i < values.length; i++)
                combo.add(CentralItemFactory.createProcessVariable(values[i]));
    }

    /** Save list values to persistent storage. */
    public void saveSettings()
    {
        IDialogSettings values = settings.addNewSection(tag);
        values.put(TAG, combo.getCombo().getItems());
    }

}
