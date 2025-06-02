import javax.swing.*; //import libs
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

class taskdata { //class for storing task info
    String name;
    String desc;
    String notes;
    int duration;

    taskdata(String name, String desc, String notes, int duration) {
        this.name = name;
        this.desc = desc;
        this.notes = notes;
        this.duration = duration;
    }

    public String toString() {
        return name + "|" + desc + "|" + notes + "|" + duration;
    }

    public static taskdata from_string(String line) {
        String[] p = line.split("\\|");
        return new taskdata(p[0], p[1], p[2], Integer.parseInt(p[3]));
    }
}

public class PomodoroApp extends JFrame { //app class

//colors
    private static final Color colorprim = new Color(40, 167, 69);
    private static final Color colorsec = new Color(240, 253, 244);
    private static final Color accent = new Color(99, 230, 190);
    private static final Color text = new Color(20, 83, 45);  
    private JTextField field_name; //button and input elements for gui
    private JTextField field_desc;
    private JTextArea field_notes;
    private JSpinner field_dur;
    private JButton btn_add;
    private JButton btn_start;
    private JButton btn_pause;
    private JButton btn_stop;
    private JComboBox<String> box_tasks;
    private JLabel lbl_timer;
    private JLabel lbl_current;
    private JProgressBar prog_bar;
    private Timer timer;
    private int secondsbank;
    private int secondssum;
    private List<taskdata> tasks;
    private File file = new File("tasks.txt");
    private boolean paused = false;

    public PomodoroApp() { //constructor
        setup_ui();
        load_file();
        lstner();
        setVisible(true);
    }

    private void setup_ui() { //ui initialization
        setTitle("Greendoro App");
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        add(createhead(), BorderLayout.NORTH);
        add(createmain(), BorderLayout.CENTER);
        add(createtimer(), BorderLayout.SOUTH);
        getContentPane().setBackground(colorsec);
    }

    private JPanel createhead() { //top header
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(colorprim);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel label = new JLabel("Pomodoro Timer", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.WHITE);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createmain() { //middle panel
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(colorsec);
        panel.add(createform());
        panel.add(createtl());
        return panel;
    }

    private JPanel createform() { //new task form
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(colorprim, 2),
                "Add New Task",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                colorprim
        ));
        panel.setBackground(Color.WHITE); //task name
        panel.add(createfield("Task Name:", field_name = new JTextField()));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createfield("Description:", field_desc = new JTextField())); //desc inputs
        panel.add(Box.createVerticalStrut(10));
        JLabel label_notes = new JLabel("Notes:");
        label_notes.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label_notes);
        field_notes = new JTextArea(3, 20);
        field_notes.setLineWrap(true);
        field_notes.setWrapStyleWord(true);
        field_notes.setBorder(BorderFactory.createLoweredBevelBorder());
        JScrollPane scroll = new JScrollPane(field_notes); //duration spinner
        panel.add(scroll);
        panel.add(Box.createVerticalStrut(10));
        JLabel label_duration = new JLabel("Duration (minutes):");
        label_duration.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label_duration);
        field_dur = new JSpinner(new SpinnerNumberModel(25, 1, 120, 1));
        field_dur.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(field_dur);
        panel.add(Box.createVerticalStrut(20));
        btn_add = createbtn("Add Task", accent); //add task button
        panel.add(btn_add);
        return panel;
    }

    private JPanel createfield(String text, JTextField field) { //field gui helper
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label, BorderLayout.NORTH);
        field.setPreferredSize(new Dimension(0, 30));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createtl() { //tasklist panel with controls
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(colorprim, 2),
                "Task List",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                colorprim
        ));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Select Task:");
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setBorder(new EmptyBorder(10, 10, 5, 10));
        panel.add(label, BorderLayout.NORTH);
        box_tasks = new JComboBox<>();
        box_tasks.setPreferredSize(new Dimension(0, 35));
        box_tasks.setBorder(new EmptyBorder(0, 10, 10, 10));
        panel.add(box_tasks, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setBorder(new EmptyBorder(10, 10, 10, 10));
        buttons.setBackground(Color.WHITE);
        btn_start = createbtn("Start", accent);
        btn_pause = createbtn("Pause", new Color(255, 193, 7));
        btn_stop = createbtn("Stop", colorprim);
        JButton btn_delete = createbtn("Delete Task", new Color(108, 117, 125));
        buttons.add(btn_start);
        buttons.add(btn_pause);
        buttons.add(btn_stop);
        buttons.add(btn_delete);
        panel.add(buttons, BorderLayout.SOUTH); //delete functionality
        btn_delete.addActionListener(e -> {
            int i = box_tasks.getSelectedIndex();
            if (i >= 0) {
                tasks.remove(i);
                box_tasks.removeItemAt(i);
                save_file();
            }
        });
        return panel;
    }

    private JPanel createtimer() { //timer
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorprim, 2),
                new EmptyBorder(20, 20, 20, 20)
        ));
        lbl_current = new JLabel("No task selected", SwingConstants.CENTER);
        lbl_current.setFont(new Font("Arial", Font.BOLD, 16));
        lbl_current.setForeground(text);
        panel.add(lbl_current, BorderLayout.NORTH);
        lbl_timer = new JLabel("00:00", SwingConstants.CENTER);
        lbl_timer.setFont(new Font("Arial", Font.BOLD, 48));
        lbl_timer.setForeground(colorprim);
        panel.add(lbl_timer, BorderLayout.CENTER);
        prog_bar = new JProgressBar(0, 100);
        prog_bar.setStringPainted(true);
        prog_bar.setString("Ready to start");
        prog_bar.setPreferredSize(new Dimension(0, 30));
        prog_bar.setForeground(accent);
        panel.add(prog_bar, BorderLayout.SOUTH);
        return panel;
    }

    private JButton createbtn(String text, Color color) { //styled button helper
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setPreferredSize(new Dimension(0, 35));
        return btn;
    }

    private void lstner() { //Attach event listeners
        btn_add.addActionListener(e -> addT());
        btn_start.addActionListener(e -> startT());
        btn_pause.addActionListener(e -> pauseT());
        btn_stop.addActionListener(e -> stopT());
        box_tasks.addActionListener(e -> updatelbl());
    }

    private void addT() { //add task
        String name = field_name.getText().trim();
        String desc = field_desc.getText().trim();
        String notes = field_notes.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task name!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int dur = (Integer) field_dur.getValue() * 60;
        taskdata task = new taskdata(name, desc, notes, dur);
        tasks.add(task);
        save_file();
        box_tasks.addItem(task.name);
        field_name.setText("");
        field_desc.setText("");
        field_notes.setText("");
        field_dur.setValue(25);
        JOptionPane.showMessageDialog(this, "Task added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startT() { //start timer
        int i = box_tasks.getSelectedIndex();
        if (i < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!paused) {
            taskdata task = tasks.get(i);
            secondsbank = task.duration;
            secondssum = task.duration;
            lbl_current.setText("Working on: " + task.name);
        }
        paused = false;
        btn_start.setEnabled(false);
        btn_pause.setEnabled(true);
        btn_stop.setEnabled(true);
        if (timer != null) timer.stop();
        timer = new Timer(1000, e -> {
            secondsbank--;
            updateT();
            if (secondsbank <= 0) {
                timer.stop();
                finishT();
            }
        });
        timer.start();
    }

    private void pauseT() { //pause timer
        if (timer != null) {
            timer.stop();
            paused = true;
            btn_start.setEnabled(true);
            btn_pause.setEnabled(false);
            prog_bar.setString("Paused");
        }
    }

    private void stopT() { //stop task and timer
        if (timer != null) timer.stop();
        resetT();
    }

    private void resetT() { //reset timer
        paused = false;
        btn_start.setEnabled(true);
        btn_pause.setEnabled(false);
        btn_stop.setEnabled(false);
        lbl_timer.setText("00:00");
        lbl_current.setText("No task selected");
        prog_bar.setValue(0);
        prog_bar.setString("Ready to start!");
    }

    private void updateT() { //uodate timer UI
        int min = secondsbank / 60;
        int sec = secondsbank % 60;
        lbl_timer.setText(String.format("%02d:%02d", min, sec));
        int p = (int) (((double) (secondssum - secondsbank) / secondssum) * 100);
        prog_bar.setValue(p);
        prog_bar.setString(p + "% Complete");
    }

    private void updatelbl() { //update label after dropdown
        int i = box_tasks.getSelectedIndex();
        lbl_current.setText(i >= 0 ? "Selected: " + tasks.get(i).name : "No task selected");
    }

    private void finishT() { //finish task
        resetT();
        closeapp();
        JOptionPane.showMessageDialog(this,
                "Task completed! Time's up!\nAll other applications have been closed.",
                "Task Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void load_file() { //load task config from txt
        tasks = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                taskdata t = taskdata.from_string(line);
                tasks.add(t);
                box_tasks.addItem(t.name);
            }
        } catch (IOException ignored) {}
    }

    private void save_file() { //save task configs to txt
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            for (taskdata t : tasks) {
                w.write(t.toString());
                w.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving tasks!", "err", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void closeapp() { //close other apps
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("taskkill /F /FI \"USERNAME eq %USERNAME%\" /FI \"IMAGENAME ne java.exe\"");
            } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("killall -u $USER");
            }
        } catch (IOException e) {
            System.err.println("Could not close other apps: " + e.getMessage());
        }
    }

    public static void main(String[] args) { //entry pt for test
        SwingUtilities.invokeLater(PomodoroApp::new);
    }
}
