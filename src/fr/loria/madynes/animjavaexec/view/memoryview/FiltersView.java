package fr.loria.madynes.animjavaexec.view.memoryview;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fr.loria.madynes.animjavaexec.execution.model.ExecutionModel;
import fr.loria.madynes.animjavaexec.execution.model.FiltersManager;

/**
 * JPanel with a TbbedPanel with JTables to display include/exclude filters.
 * 
 * @author andrey
 *
 */
public class FiltersView extends JPanel {
	private static final long serialVersionUID = 1L;
	private FiltersManager filtersManager;
		
	/**
	 * As filters can be initialized before execution, constructor needs to get
	 * the target filters manager (contrary to other viewq there observable
	 * managers can be set later and there events coming from these observables provide
	 * enough informations to display things on time.
	 * 
	 * @param filtersManager
	 */
	public FiltersView(FiltersManager filtersManager){
		super(new GridLayout(1, 1));
		assert filtersManager!=null;
		setFiltersManager(filtersManager); 
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Include filters", new FiltersPanel(true)); //TODO: use bundle
        tabbedPane.addTab("Exclude filters", new FiltersPanel(false)); //TODO: use bundle
        add(tabbedPane);

	}

	private void setFiltersManager(FiltersManager filtersManager) {
		this.filtersManager = filtersManager;
	}

	public FiltersManager getFiltersManager() {
		return filtersManager;
	}
	
	private  class FiltersPanel extends JPanel implements ListSelectionListener {
		private static final long serialVersionUID = 1L;
		private boolean forIncludeFilters;
		private DefaultListModel listModel;
		JList list;
		JButton removeButton;
		JButton changeButton;
		JButton addButton;
		JTextField filterTextField;
		private FiltersPanel(boolean forIncludeFilters){
			super(new BorderLayout());
			this.forIncludeFilters=forIncludeFilters;
			

	        listModel = new DefaultListModel();
	        String[] intitFilters=this.forIncludeFilters?filtersManager.getIncludeFilters():filtersManager.getExcludeFilters();
	        for (String f:intitFilters){
	        	listModel.addElement(f);
	        }
	        //Create the list and put it in a scroll pane.
	        list = new JList(listModel);
	        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        list.setSelectedIndex(0);
	        list.addListSelectionListener(this);
	        list.setVisibleRowCount(5);
	        JScrollPane listScrollPane = new JScrollPane(list);

	        removeButton = new JButton("Remove Filter"); // todo: in bundle...
	        removeButton.setActionCommand("removeListener");
	        removeButton.addActionListener(new RemoveButtonListener());
	        removeButton.setEnabled(false);
	        removeButton.setActionCommand("removeFilter");
	        
	        changeButton = new JButton("Change");
	        ChangeButtonListener changeButtonListener=new ChangeButtonListener();
	        changeButton.addActionListener(changeButtonListener);
	        changeButton.setActionCommand("changeFilter");
	        
	        addButton=new JButton("Add new");
	        AddButtonListener addButtonListener=new AddButtonListener();
	        addButton.addActionListener(addButtonListener);
	        addButton.setActionCommand("addNewFilter");
	        
	        filterTextField = new JTextField(50);
	        filterTextField.addActionListener(changeButtonListener); // button action is changed
	        filterTextField.getDocument().addDocumentListener(new FilterTextListener());
	        filterTextField.setEditable(true);
	        filterTextField.setEnabled(true);
	        filterTextField.requestFocusInWindow();

	        //Create a panel that uses BoxLayout.
	        JPanel buttonPane = new JPanel();
	        buttonPane.setLayout(new BoxLayout(buttonPane,
	                                           BoxLayout.LINE_AXIS));
	        buttonPane.add(removeButton);
	        buttonPane.add(Box.createHorizontalStrut(5));
	        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
	        buttonPane.add(Box.createHorizontalStrut(5));
	        buttonPane.add(filterTextField);
	        buttonPane.add(changeButton);
	        buttonPane.add(addButton);
	        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	        add(listScrollPane, BorderLayout.CENTER);
	        add(buttonPane, BorderLayout.PAGE_END);
	        // update text field and enable buttons
	        valueChanged(null);
		}
		
		private void addChangeButtonsState(){
			boolean enabled=(filterTextField.getText().trim().length()>0);
			changeButton.setEnabled(enabled);
			addButton.setEnabled(enabled);	
		}
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e==null || e.getValueIsAdjusting()){
				int selectedIdx=list.getSelectedIndex();
				if (selectedIdx>=0){
					filterTextField.setText(listModel.getElementAt(selectedIdx).toString());
					filterTextField.requestFocusInWindow();
					//changeButton.setEnabled(true);
					removeButton.setEnabled(true);
					addChangeButtonsState();
				}else{
					// no more selection...
					//filterTextField.setText(null);
					changeButton.setEnabled(false);
					removeButton.setEnabled(false);
				}
			}
		}
		private class RemoveButtonListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedIdx=list.getSelectedIndex();
			    if (selectedIdx>=0){ // button enabling should ensure this to true...
			    	listModel.remove(selectedIdx);
			    	int listSize=listModel.size();
			    	if (listSize==0){
			    		// list now empty => disable removeButton
			    		//changeButton.setEnabled(false);
			    		//removeButton.setEnabled(false);
			    	}else{// ensure selection...
			    		if (selectedIdx>=listSize){ // we just deleted last elt...
			    			--selectedIdx;
			    		}
			    		list.setSelectedIndex(selectedIdx);
			    		list.ensureIndexIsVisible(selectedIdx);
			    	}
			    }
			    valueChanged(null);
			}
		}
		private class ChangeButtonListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource()!=changeButton){
					// give feedback to user
					changeButton.doClick(); // BEWARE: this REALLY actions the button so DO NOT DO useful
											// things here, if so they would  be done twice !!
				}else{
					int selectedIdx=list.getSelectedIndex();
					if (selectedIdx>=0){ // button enabling should ensure this to true...
						String s=filterTextField.getText().trim();
			    		if (s.length()>0){ // add/change button enabling should ensure that...
			    			listModel.set(selectedIdx, s);
			    		}
					}
				}
			}
		}
		private class AddButtonListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				String s=filterTextField.getText().trim();
		    	if (s.length()>0){ // add/change button enabling should ensure that...
		    		 int addAtIndex = list.getSelectedIndex(); //get selected index
		             if (addAtIndex == -1) { //no selection, so insert at beginning
		            	 addAtIndex = 0;
		             } else {           //add after the selected item
		            	 ++addAtIndex;
		             }
		             listModel.add(addAtIndex, s);
		    	}
			}
		}
		private class FilterTextListener implements DocumentListener {

			@Override // DocumentListener
			public void changedUpdate(DocumentEvent e) {
				addChangeButtonsState();
			}

			@Override // DocumentListener
			public void insertUpdate(DocumentEvent e) {
				addChangeButtonsState();
			}

			@Override // DocumentListener
			public void removeUpdate(DocumentEvent e) {
				addChangeButtonsState();
			}
		}
	}
}
