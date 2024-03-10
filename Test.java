import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Test {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing State Saving Example");
        JTextField textField = new JTextField(20);
        JButton saveButton = new JButton("Save State");
        JButton loadButton = new JButton("Load State");

        // Saveボタンのアクション
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveState(textField.getText());
            }
        });

        // Loadボタンのアクション
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String savedState = loadState();
                textField.setText(savedState);
            }
        });

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(textField);
        frame.add(saveButton);
        frame.add(loadButton);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static void saveState(String state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("saved_state.dat"))) {
            oos.writeObject(state);
            System.out.println("State saved successfully.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String loadState() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("saved_state.dat"))) {
            return (String) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
