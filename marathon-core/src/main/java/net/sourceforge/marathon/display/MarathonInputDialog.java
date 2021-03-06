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
package net.sourceforge.marathon.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sourceforge.marathon.runtime.api.ButtonBarFactory;
import net.sourceforge.marathon.runtime.api.EscapeDialog;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public abstract class MarathonInputDialog extends EscapeDialog {
    private static final long serialVersionUID = 1L;
    private boolean ok;
    private JTextField inputField;
    private JLabel errorMsgLabel;
    private JButton okButton;

    private JButton cancelButton;

    public MarathonInputDialog(JFrame parent, String title) {
        super(parent, title, true);
        initialize();
    }

    private void initialize() {
        FormLayout layout = new FormLayout("3dlu, pref, 3dlu, pref:grow", "pref, 3dlu, pref, 3dlu, pref, fill:4dlu");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.border(Borders.DIALOG);

        CellConstraints cc = new CellConstraints();
        CellConstraints cc1 = new CellConstraints();

        errorMsgLabel = new JLabel("");
        errorMsgLabel.setIcon(new ImageIcon(MarathonInputDialog.class.getClassLoader()
                .getResource("net/sourceforge/marathon/display/icons/enabled/error.gif")));
        errorMsgLabel.setVisible(false);

        int row = 1;
        inputField = new JTextField(15);
        builder.addLabel(getFieldLabel(), cc.xy(2, row), inputField, cc1.xy(4, row));
        row += 2;
        okButton = createOKButton();
        okButton.setEnabled(false);
        ok = false;
        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                String errorMessage = validateInput(inputField.getText());
                if (errorMessage != null) {
                    errorMsgLabel.setText(errorMessage);
                    okButton.setEnabled(false);
                    errorMsgLabel.setVisible(true);
                } else {
                    okButton.setEnabled(true);
                    errorMsgLabel.setVisible(false);
                }
            }

            public void insertUpdate(DocumentEvent e) {
                String errorMessage = validateInput(inputField.getText());
                if (errorMessage != null) {
                    errorMsgLabel.setText(errorMessage);
                    okButton.setEnabled(false);
                    errorMsgLabel.setVisible(true);
                } else {
                    okButton.setEnabled(true);
                    errorMsgLabel.setVisible(false);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                String errorMessage = validateInput(inputField.getText());
                if (errorMessage != null) {
                    errorMsgLabel.setText(errorMessage);
                    okButton.setEnabled(false);
                    errorMsgLabel.setVisible(true);
                } else {
                    okButton.setEnabled(true);
                    errorMsgLabel.setVisible(false);
                }
            }

        };
        inputField.getDocument().addDocumentListener(documentListener);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok = true;
                dispose();
            }
        });
        cancelButton = createCancelButton();
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        builder.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), cc.xyw(2, row, 3));
        builder.add(errorMsgLabel, cc.xyw(1, row + 2, 4));

        getContentPane().add(builder.getPanel());

        pack();
        setLocationRelativeTo(getParent());
    }

    protected abstract String getFieldLabel();

    protected abstract JButton createOKButton();

    protected abstract JButton createCancelButton();

    protected abstract String validateInput(String inputText);

    public boolean isOk() {
        return ok;
    }

    public String getValue() {
        return inputField.getText();
    }

    public void setValue(String text) {
        inputField.setText(text);
    }

    public JTextField getInputField() {
        return inputField;
    }

    @Override public JButton getOKButton() {
        return okButton;
    }

    @Override public JButton getCloseButton() {
        return cancelButton;
    }
}
