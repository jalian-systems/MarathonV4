/*******************************************************************************
 * Copyright 2016 Jalian Systems Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.sourceforge.marathon.checklist;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import net.sourceforge.marathon.checklist.CheckListForm.Mode;
import net.sourceforge.marathon.runtime.api.EscapeDialog;
import net.sourceforge.marathon.runtime.api.UIUtils;

public class MarathonCheckList extends EscapeDialog {
    private final File checklistDir;

    private final class CheckListFileModel extends AbstractListModel<File> {
        private static final long serialVersionUID = 1L;
        File[] items;

        private CheckListFileModel() {
            initItems();
        }

        private void initItems() {
            if (checklistDir != null)
                items = checklistDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if (dir.equals(checklistDir) && name.endsWith(".xml"))
                            return true;
                        return false;
                    }
                });
            else
                items = new File[0];
        }

        public File getElementAt(int index) {
            return items[index];
        }

        public int getSize() {
            return items.length;
        }

        public void reset() {
            initItems();
            fireContentsChanged(this, 0, items.length);
        }
    }

    private static final long serialVersionUID = 1L;
    private JList<File> list;
    private CheckListForm formPanel;
    private JButton createButton;
    private JButton editButton;
    private JButton okButton;
    private CheckListFileModel model;
    private File selectedFile;
    private JSplitPane splitPane;
    private boolean ok = false;
    private JList<File> checkList;
    private final boolean insert;
    private JButton cancel;

    public MarathonCheckList(JFrame parent, File checkListDir, boolean insert) {
        super(parent, insert ? "Select a Checklist" : "Manage Checklists", true);
        this.checklistDir = checkListDir;
        this.insert = insert;
        JPanel panel = new JPanel(new BorderLayout());
        JSplitPane splitPane = createSplitPane();
        panel.add(splitPane, BorderLayout.CENTER);
        JPanel buttonPanel = createButtonPanel();
        panel.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(panel);
        setSize(750, 600);
        setLocationRelativeTo(parent);
    }

    private JPanel createButtonPanel() {
        createButton = UIUtils.createNewButton();
        createButton.setMnemonic(KeyEvent.VK_N);
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final CheckList checklist = new CheckList();
                CheckListForm form = new CheckListForm(checklist, Mode.EDIT);
                final CheckListDialog dialog = new CheckListDialog(MarathonCheckList.this, form);
                JButton saveAction = UIUtils.createSaveButton();
                saveAction.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser(checklistDir);
                        if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();
                            if (!file.getName().endsWith(".xml")) {
                                file = new File(file.getParentFile(), file.getName() + ".xml");
                            }
                            if (file.exists()) {
                                int ret = JOptionPane.showConfirmDialog(dialog,
                                        "File " + file.getName() + " Exists. Do you want to overwrite", "Checklist Exists",
                                        JOptionPane.YES_NO_CANCEL_OPTION);
                                if (ret == JOptionPane.CANCEL_OPTION || ret == JOptionPane.NO_OPTION)
                                    return;
                            }
                            try {
                                checklist.save(new FileOutputStream(file));
                            } catch (FileNotFoundException e1) {
                                JOptionPane.showMessageDialog(dialog, "Unable to save the file " + file);
                                return;
                            }
                            dialog.dispose();
                            model.reset();
                            list.setSelectedValue(file, true);
                        }
                    }
                });
                JButton cancelAction = UIUtils.createCancelButton();
                cancelAction.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (dialog.isDirty()) {
                            if (JOptionPane.showConfirmDialog(dialog, "Your modifications will be lost. Do you want to continue?",
                                    "Abort", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                                return;
                        }
                        dialog.dispose();
                    }
                });
                dialog.setActionButtons(new JButton[] { saveAction, cancelAction });
                dialog.setTitle("Create a New Checklist");
                dialog.setVisible(true);
                model.reset();
            }
        });
        editButton = UIUtils.createEditButton();
        editButton.setMnemonic(KeyEvent.VK_E);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final CheckListForm form = getSelectedCheckListForm(Mode.EDIT);
                if (form == null)
                    return;
                final CheckListDialog dialog = new CheckListDialog(MarathonCheckList.this, form);
                JButton saveAsAction = UIUtils.createSaveAsButton();
                saveAsAction.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser(checklistDir);
                        if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();
                            if (!file.getName().endsWith(".xml")) {
                                file = new File(file.getParentFile(), file.getName() + ".xml");
                            }
                            if (file.exists()) {
                                int ret = JOptionPane.showConfirmDialog(dialog,
                                        "File " + file.getName() + " Exists. Do you want to overwrite", "Checklist Exists",
                                        JOptionPane.YES_NO_CANCEL_OPTION);
                                if (ret == JOptionPane.CANCEL_OPTION || ret == JOptionPane.NO_OPTION)
                                    return;
                            }
                            try {
                                form.getCheckList().save(new FileOutputStream(file));
                            } catch (FileNotFoundException e1) {
                                JOptionPane.showMessageDialog(dialog, "Unable to save the file " + file);
                                return;
                            }
                            dialog.dispose();
                            model.reset();
                            list.setSelectedValue(file, true);
                        }
                    }
                });
                JButton saveAction = UIUtils.createSaveButton();
                saveAction.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            form.getCheckList().save(new FileOutputStream(selectedFile));
                        } catch (FileNotFoundException e1) {
                            JOptionPane.showMessageDialog(dialog, "Unable to save the file " + selectedFile);
                            return;
                        }
                        dialog.dispose();
                        model.reset();
                        list.removeSelectionInterval(list.getSelectedIndex(), list.getSelectedIndex());
                        list.setSelectedValue(selectedFile, true);
                    }
                });
                JButton cancelAction = UIUtils.createCancelButton();
                cancelAction.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (dialog.isDirty()) {
                            if (JOptionPane.showConfirmDialog(dialog, "Your modifications will be lost. Do you want to continue?",
                                    "Abort", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                                return;
                        }
                        dialog.dispose();
                    }
                });
                dialog.setActionButtons(new JButton[] { saveAction, saveAsAction, cancelAction });
                dialog.setTitle("Modify Checklist");
                dialog.setVisible(true);
            }
        });
        if (insert)
            okButton = UIUtils.createInsertButton();
        else {
            okButton = UIUtils.createDoneButton();
        }
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok = true;
                dispose();
            }
        });
        cancel = UIUtils.createCancelButton();
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok = false;
                dispose();
            }
        });
        editButton.setEnabled(false);
        if (insert)
            okButton.setEnabled(false);
        ButtonBarBuilder builder = new ButtonBarBuilder();
        builder.addGlue();
        builder.addButton(createButton);
        builder.addRelatedGap();
        builder.addButton(editButton);
        builder.addUnrelatedGap();
        builder.addButton(okButton);
        if (insert) {
            builder.addButton(cancel);
        }
        return builder.getPanel();
    }

    private JSplitPane createSplitPane() {
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(150);
        splitPane.setOneTouchExpandable(true);
        list = getCheckList();
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                CheckListForm form = getSelectedCheckListForm(Mode.DISPLAY);
                if (form == null) {
                    editButton.setEnabled(false);
                    okButton.setEnabled(false);
                    return;
                }
                editButton.setEnabled(true);
                okButton.setEnabled(true);
                updateSplitPane(form);
            }

        });
        splitPane.setLeftComponent(new JScrollPane(list));
        splitPane.setRightComponent(new JPanel());
        return splitPane;
    }

    private void updateSplitPane(CheckListForm form) {
        int deviderLocation = splitPane.getDividerLocation();
        splitPane.setRightComponent(new JScrollPane(form));
        splitPane.setDividerLocation(deviderLocation);
    }

    private CheckListForm getSelectedCheckListForm(Mode mode) {
        int index = list.getSelectedIndex();
        if (index == -1)
            return null;
        selectedFile = (File) list.getModel().getElementAt(index);
        CheckListForm showFrom = createCheckList(selectedFile, mode);
        return showFrom;
    }

    private CheckListForm createCheckList(File file, Mode mode) {
        try {
            formPanel = new CheckListForm(CheckList.read(file), mode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return formPanel;
    }

    private JList<File> getCheckList() {
        checkList = new JList<File>();
        checkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        checkList.setBorder(BorderFactory.createTitledBorder("Check Lists"));
        model = new CheckListFileModel();
        checkList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                String name = ((File) value).getName();
                name = name.substring(0, name.length() - 4);
                return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
            }
        });
        checkList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return;
                if (checkList.getSelectedIndex() == -1)
                    okButton.setEnabled(false);
                else
                    okButton.setEnabled(true);
            }
        });
        checkList.setModel(model);
        return checkList;
    }

    public boolean isOK() {
        return ok;
    }

    public File getSelectedChecklist() {
        return (File) checkList.getSelectedValue();
    }

    @Override public JButton getOKButton() {
        return null;
    }

    @Override public JButton getCloseButton() {
        return cancel;
    }

}
