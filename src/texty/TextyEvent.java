package texty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Steve Karwacki
 */
public class TextyEvent {
    
    // Access to Model and View
    TextyModel textyModel;
    Texty textyView;
    
    public TextyEvent(Texty gui, TextyModel model) {
        textyView = gui; 
        textyModel = model;
    }
    
    // Event Handlers
    class SaveEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String[] fileLocation;
            switch(command){
                case "InitSave":
                    saveFile();
                    break;
                case "SaveNew":
                    fileLocation = textyView.saveWin.getFileLocation();
                    saveNewFile(fileLocation);
                    break;
                case "SaveAnyway":
                    fileLocation = textyView.saveWin.getFileLocation();
                    saveFileAnyway(fileLocation);
                    break;
                case "CancelSaveAnyway":
                    textyView.saveAnywayWin.dispatchEvent(new WindowEvent(textyView.saveAnywayWin, WindowEvent.WINDOW_CLOSING));
                    textyView.saveAnywayWin.dispose();
                    break;
            }
        }
    }

    class OpenEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String[] fileLocation;
            switch(command){
                case "InitOpen":
                    textyView.openWin = textyView.new OpenFileWin();
                    break;
                case "Open":
                    fileLocation = textyView.openWin.getFileLocation();
                    openFile(fileLocation);
                    break;
            }
        }
    }

    class NewEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch(command){
                case "InitNew":
                    TextyModel textyEditor = new TextyModel();
                    break;
            }
        }
    }

    class RenameEvent implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            String newFilename;
            switch(command){
                case "InitRename":
                    textyView.renameWin = textyView.new RenameFileWin();
                    break;
                case "Rename":
                    newFilename = textyView.renameWin.getNewFilename();
                    renameFile(newFilename);
                    break;
            }
        }
    }

    // Helper methods
    public static boolean containsRegxChars(String haystack, String needle) {
        Pattern pattern = Pattern.compile(needle);
        Matcher matcher = pattern.matcher(haystack);
        return matcher.find();
    }

    // Body of methods
    private void openFile(String[] fileLocation) {
        String filepath = fileLocation[0];
        String filename = fileLocation[1];
        File file = new File(filepath + filename);
        String content = "";

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String text;

            while ((text = reader.readLine()) != null) {
                content += text;
            }

            textyView.openWin.dispatchEvent(new WindowEvent(textyView.openWin, WindowEvent.WINDOW_CLOSING));
            textyView.openWin.dispose();
            TextyModel.globalFilepath = filepath;

            TextyModel textyEditor = new TextyModel(fileLocation, false);
            textyEditor.textyView.textarea.setText(content);

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(textyView, "File Not Found!\nError: The file \"" + filepath + filename + "\" does not exist", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(textyView, "File Could Not Be Opened!\nError: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean saveFile() { 
        String filepath;
        boolean saveSuccess = false;
        if(textyModel.fileIsNew) {
            textyView.saveWin = textyView.new saveLocationWin();
        }
        else {

            filepath = textyModel.getFilepath() + textyModel.getFilename();

            File file = new File(filepath);
            String content = textyView.textarea.getText();

            try(FileOutputStream fop = new FileOutputStream(file)) {

                if(!file.exists()) {
                    file.createNewFile();
                }

                byte[] contentInBytes = content.getBytes();
                fop.write(contentInBytes);

                JOptionPane.showMessageDialog(textyView, "File Saved!", "Success", JOptionPane.PLAIN_MESSAGE);

                saveSuccess = true;

            } catch(IOException e) {
                JOptionPane.showMessageDialog(textyView, "File was not saved!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return saveSuccess;
    }
    
    private void saveNewFile(String[] fileLocation) {
        try {
            String filepath = fileLocation[0];
            String filename = fileLocation[1];
            String fullFilepath = filepath + filename;

            File file = new File(fullFilepath);
            
            if(file.exists()) {
                textyView.saveAnywayWin = textyView.new SaveAnywayWin();
            }
            else {
                textyModel.setFilepath(filepath);
                textyModel.setFilename(filename);
                textyModel.fileIsNew = false;
                textyView.saveWin.setVisible(false);

                if(saveFile()) {
                    textyView.setTitle("Texty - " + filename);
                    textyView.saveWin.dispatchEvent(new WindowEvent(textyView.saveWin, WindowEvent.WINDOW_CLOSING));
                    textyView.saveWin.dispose();
                    TextyModel.globalFilepath = filepath;
                }
                else {
                    textyView.saveWin.setVisible(true);
                }
            }

        } catch(Exception e) {
        }
    }

    private void saveFileAnyway(String[] fileLocation) { 
        try {
            String filepath = fileLocation[0];
            String filename = fileLocation[1];
            textyModel.setFilepath(filepath);
            textyModel.setFilename(filename);
            textyModel.fileIsNew = false;
            
            textyView.saveAnywayWin.setVisible(false);
            textyView.saveWin.setVisible(false);
            
            if(saveFile()) {
                textyView.saveAnywayWin.dispatchEvent(new WindowEvent(textyView.saveAnywayWin, WindowEvent.WINDOW_CLOSING));
                textyView.saveAnywayWin.dispose();
                textyView.saveWin.dispatchEvent(new WindowEvent(textyView.saveWin, WindowEvent.WINDOW_CLOSING));
                textyView.saveWin.dispose();
                TextyModel.globalFilepath = filepath;
            }
            else {
                textyView.saveAnywayWin.setVisible(true);
                textyView.saveWin.setVisible(true);
            }
            
        } catch(Exception e) {
        }
    }

    private void renameFile(String filename) {
        try {
            textyModel.setFilename(filename);
            textyModel.fileIsNew = true;
            textyView.setTitle("Texty - " + filename);
            
            textyView.renameWin.dispatchEvent(new WindowEvent(textyView.renameWin, WindowEvent.WINDOW_CLOSING));
            textyView.renameWin.dispose();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(textyView, "File could not be renamed!\nError: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}