import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.Arrays;
import java.util.Comparator;


class Data implements Serializable {
    Game G;

    public Data(Game G) {
        this.G = G;
    }

    Game get_game() {
        return G;
    }

    Game.Display get_display() {
        return G.D;
    }
}

class GameSaveLoad {
    public static void saveProgress(Game G) {
        Data progress = new Data(G);
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("save.dat"))) {
            outputStream.writeObject(progress);
            System.out.println("Game progress saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Data loadProgress() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("save.dat"))) {
            return (Data) inputStream.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No saved data found.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class Game implements Serializable {
    Display D;
    Party P;
    Enemy[] enemys;
    Stage[] stages;
    int stage_num;
    int enemy_num;
    int current_stage;
    Field current_fields;
    public volatile int yes_no_selected;
    public volatile int command_selected;
    public volatile int enemy_selected;
    public volatile int stage_selected;
    public volatile int field_selected;
    static final Object lock = new Object();
    static final Object lock2 = new Object();
    static final Object lock3 = new Object();
    volatile boolean buttonClicked;
    String inputText;
    Battle current_battle;

    Game() {
        this.stages = new Stage[8];
        this.D = new Display();
        this.stage_num = 1;
        this.buttonClicked = false;
    }

    void start_story() {
        D.start_game();
        
        try {
            waitForButtonClick();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    void first() {
        Event type1 = new addType1();
        type1.triger();
        this.stages[0] = new Stage1();
        this.stage_num = 1;
    }

    void waitForButtonClick() throws InterruptedException {
        buttonClicked = false;
        synchronized(lock) {
            do {
                try {
                    buttonClicked = false;
                    lock.wait();
                } catch (InterruptedException ex) {
                    System.out.println("Interrupted");
                }
            } while (!buttonClicked);
        }
        buttonClicked = false;
    }

    public void stage_select() {
        stages[0].fields[0].action();
        while (true) {
            D.move_bottunS("移動先");
            try {
                waitForButtonClick();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            D.all_button_invisible();
            current_stage = stage_selected;

            D.move_bottunF("移動先");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            D.all_button_invisible();
            stages[current_stage].fields[field_selected].action();
        }
    }

    int rand(int max_num) {
        Random random = new Random();
        int random_num = random.nextInt(max_num);
        return random_num;
    }

    // 表示クラス
    class Display implements ActionListener, Serializable
    {

        // 画面全体
        static JFrame disp;
        // パネル
        static JPanel left_panel, right_panel, center_panel, bottom_Panel, inner_Panel, enemy_name, enemy_status;
        static JPanel[] status_M;
        static JPanel[] status_S;
        // メッセージ表示ラベルオブジェクト
        static JLabel msg_lbl1, msg_lbl2, msg_lbl3, msg_lbl4;
        static JLabel msg_battle;
        // ステータス表示ラベルオブジェクト
        static JLabel[] name_lbl, hp_lbl, mp_lbl,lv_lbl;
        // 相手の情報ラベルオブジェクト
        static JLabel[] enemy_hp_lbl, enemy_name_lbl;
        // 選択ボタンオブジェクト
        static JButton[] btn, btn2;
        private JButton start, finish;
        String input;
        
        // コンストラクタ(初期化処理)
        Display()
        {
            disp = new JFrame("RPG");  // 画面を生成
            disp.setSize(1400, 850);           // 表示サイズを設定
            disp.setLocationRelativeTo(null); // 画面の表示位置を中央に設定
            disp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 「×」ボタンで画面を閉じるように設定
            disp.setResizable(true);         // 画面サイズを変更できないように設定
            disp.setVisible(true);
            
            disp.setLayout(new GridLayout(2, 1));
            // ゲーム画面を表示

        }

        //ゲーム開始
        void start_game() {
            start = new JButton("ゲームを始める");
            disp.add(start); // ボトムパネル左側にボタンを追加
            start.setFont( new Font("游明朝 Regular", Font.PLAIN, 100) );
            start.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    disp.remove(start);
                    disp.remove(finish);
                    
                    start = new JButton("はじめから");
                    disp.add(start); // ボトムパネル左側にボタンを追加
                    start.setFont( new Font("游明朝 Regular", Font.PLAIN, 100) );
                    start.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized(lock) {
                                disp.remove(start);
                                disp.remove(finish);
                                disp.setVisible(true);
                                gui1();
                                command_selected = 1;
                                buttonClicked = true;
                                lock.notify();
                            }
                        }
                    });
                    
                    
                    finish = new JButton("つづきから");
                    disp.add(finish); // ボトムパネル左側にボタンを追加
                    finish.setFont( new Font("游明朝 Regular", Font.PLAIN, 100) );
                    finish.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized(lock) {
                                disp.remove(start);
                                disp.remove(finish);
                                disp.setVisible(true);
                                gui1();
                                command_selected = 2;
                                buttonClicked = true;
                                lock.notify();
                            }
                        }
                    });
                }
            });

            finish = new JButton("終了");
            disp.add(finish); // ボトムパネル左側にボタンを追加
            finish.setFont( new Font("游明朝 Regular", Font.PLAIN, 100) );
            finish.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    finish();
                }
            });
            

            disp.setVisible(true);

        }

        //ステータスの表示
        public void status_main() {
            int roop_count = 4;
            if (P.member < 4) {
                roop_count = P.member;
            }
            left_panel.removeAll();
            left_panel.revalidate();
            left_panel.repaint();
            disp.add( left_panel, BorderLayout.WEST);
            for (int i=0; i < roop_count; i++) {
                status_M[i].setLayout(new GridLayout(4, 1));
                name_lbl[i].setText(P.main[i].name);
                hp_lbl[i].setText("HP: " + Integer.toString(P.main[i].HP) + " / " + Integer.toString(P.main[i].max_HP));
                mp_lbl[i].setText("MP: " + Integer.toString(P.main[i].MP) + " / " + Integer.toString(P.main[i].max_MP));
                lv_lbl[i].setText("Lv: " + Integer.toString(P.main[i].Lv));
                setLabelFont(name_lbl[i], Color.WHITE, 10, 10, 600, 25, 20, false);
                setLabelFont(hp_lbl[i], Color.WHITE, 10, 10, 600, 25, 20, false);
                setLabelFont(mp_lbl[i], Color.WHITE, 10, 10, 600, 25, 20, false);
                setLabelFont(lv_lbl[i], Color.WHITE, 10, 10, 600, 25, 20, false);
                status_M[i].add(name_lbl[i]);
                status_M[i].add(hp_lbl[i]);
                status_M[i].add(mp_lbl[i]);
                status_M[i].add(lv_lbl[i]);
                left_panel.add(status_M[i]);
            }
            disp.repaint();
        }

        public void status_sub() {
            int roop_count = P.member-4;
            left_panel.removeAll();
            left_panel.revalidate();
            left_panel.repaint();
            disp.add( left_panel, BorderLayout.WEST);
            for (int i=0; i < roop_count; i++) {
                status_S[i].setLayout(new GridLayout(4, 1));
                name_lbl[i].setText(P.sub[i].name);
                hp_lbl[i].setText("HP: " + Integer.toString(P.sub[i].HP) + " / " + Integer.toString(P.sub[i].max_HP));
                mp_lbl[i].setText("MP: " + Integer.toString(P.sub[i].MP) + " / " + Integer.toString(P.sub[i].max_MP));
                lv_lbl[i].setText("Lv: " + Integer.toString(P.sub[i].Lv));
                setLabelFont(name_lbl[i], Color.WHITE, 10, 10, 600, 25, 20, false);
                setLabelFont(hp_lbl[i], Color.WHITE, 10, 10, 600, 25, 20, false);
                setLabelFont(mp_lbl[i], Color.WHITE, 10, 10, 600, 25, 20, false);
                setLabelFont(lv_lbl[i], Color.WHITE, 10, 10, 600, 25, 20, false);
                status_S[i].add(name_lbl[i]);
                status_S[i].add(hp_lbl[i]);
                status_S[i].add(mp_lbl[i]);
                status_S[i].add(lv_lbl[i]);
                left_panel.add(status_S[i]);
            }
            disp.repaint();
        }

        //メンバー追加
        void new_member(Game.AddPlayer eve) {
            
            JTextField textField = new JTextField(20);
            JButton submitButton = new JButton("入力");
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (lock) {
                        
                        inputText = textField.getText();
                        eve.executeP(inputText);
                        buttonClicked = true;
                        lock.notify();
                    }
                    
                }
            });
            textField.setColumns(10);
            textField.setFont(new Font("游明朝 Regular", Font.PLAIN, 50));
            inner_Panel.setLayout(new BorderLayout());
            inner_Panel.add(textField,BorderLayout.CENTER);
            inner_Panel.add(submitButton,BorderLayout.EAST);
            
            disp.repaint();
        
        }

        void validall() {
            for (int i=0;i<4;i++) {
                status_M[i].revalidate();
                status_M[i].repaint();
                status_S[i].revalidate();
                status_S[i].repaint();
            }
            right_panel.revalidate();
            right_panel.repaint();
            left_panel.revalidate();
            left_panel.repaint();
            center_panel.revalidate();
            center_panel.repaint();
            bottom_Panel.revalidate();
            bottom_Panel.repaint();
            inner_Panel.revalidate();
            inner_Panel.repaint();
            enemy_name.revalidate();
            enemy_name.repaint();
            enemy_status.revalidate();
            enemy_status.repaint();
            
        }
        //基本GUIの表示
        void gui1() {
            disp.setLayout(new BorderLayout());
            
            // 左パネルの表示設定
            left_panel = new JPanel(); // パネルを生成
            left_panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            setPanel(left_panel, Color.BLACK, null, new Dimension(210, 850) ); // パネルの背景色、レイアウト、サイズを設定
            disp.add( left_panel, BorderLayout.WEST); // 画面中央部にパネルを追加
            left_panel.setLayout(new GridLayout(4, 1));
            left_panel.revalidate();
            left_panel.repaint();

            // 中央パネルの表示設定
            center_panel = new JPanel(); // パネルを生成
            center_panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            setPanel(center_panel, Color.BLACK, null, new Dimension(980, 850) ); // パネルの背景色、レイアウト、サイズを設定
            disp.add(center_panel, BorderLayout.CENTER); // 画面中央部にパネルを追加

            center_panel.setLayout(new GridLayout(4, 1));
            center_panel.revalidate();
            center_panel.repaint();

            // 左パネルの表示設定
            right_panel = new JPanel(); // パネルを生成
            right_panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            setPanel(right_panel, Color.BLACK, null, new Dimension(210, 850) ); // パネルの背景色、レイアウト、サイズを設定
            disp.add( right_panel, BorderLayout.EAST); // 画面中央部にパネルを追加
            right_panel.setLayout(new GridLayout(9, 1));

            enemy_name = new JPanel();
            setPanel(enemy_name, Color.BLACK, null, new Dimension(700, 200) );
            center_panel.add(enemy_name);

            enemy_status = new JPanel();
            setPanel(enemy_status, Color.BLACK, null, new Dimension(700, 200) );
            center_panel.add(enemy_status);

            inner_Panel = new JPanel();
            
            inner_Panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            setPanel(inner_Panel, Color.BLACK, null, new Dimension(700, 300) );
            center_panel.add(inner_Panel);

            // 下部パネルの表示設定
            bottom_Panel = new JPanel(); // パネルを生成
            bottom_Panel.setLayout(new BorderLayout());
            bottom_Panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            setPanel(bottom_Panel, Color.BLACK, null, new Dimension(700, 300) );
            bottom_Panel.setLayout(new GridLayout(4, 1));
            center_panel.add(bottom_Panel);

            msg_lbl1 = new JLabel("");
            setLabelFont(msg_lbl1, Color.WHITE, 10, 10, 600, 25, 20, false); // ラベルのフォント設定
            bottom_Panel.add(msg_lbl1);

            msg_lbl2 = new JLabel("");
            setLabelFont(msg_lbl2, Color.WHITE, 10, 40, 600, 25, 20, false); // ラベルのフォント設定
            bottom_Panel.add(msg_lbl2);

            msg_lbl3 = new JLabel("");
            setLabelFont(msg_lbl3, Color.WHITE, 10, 70, 600, 25, 20, false); // ラベルのフォント設定
            bottom_Panel.add(msg_lbl3);

            msg_lbl4 = new JLabel("");
            setLabelFont(msg_lbl4, Color.WHITE, 10, 70, 600, 25, 20, false); // ラベルのフォント設定
            bottom_Panel.add(msg_lbl4);

            status_M = new JPanel[4];
            status_S = new JPanel[4];
            name_lbl = new JLabel[8];
            hp_lbl = new JLabel[8];
            mp_lbl = new JLabel[8];
            lv_lbl = new JLabel[8];
            enemy_hp_lbl = new JLabel[4];
            enemy_name_lbl = new JLabel[4];
            msg_battle=new JLabel("");
            setLabelFont(msg_battle, Color.WHITE, 0, 0, 300, 25, 20, false);
            //msg_battle.setText("コマンド");
            
            //right_panel.add(msg_battle);
            for (int i=0; i < 4; i++) {

                status_M[i] = new JPanel(); // パネルを生成
                setPanel(status_M[i], Color.BLACK, null, new Dimension(210, 220) ); // パネルの背景色、レイアウト、サイズを設定
                status_M[i].setLayout(new GridLayout(4, 1));
                status_S[i] = new JPanel(); // パネルを生成
                setPanel(status_S[i], Color.BLACK, null, new Dimension(210, 220) ); // パネルの背景色、レイアウト、サイズを設定
                status_S[i].setLayout(new GridLayout(4, 1));
                status_S[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                status_M[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                enemy_hp_lbl[i] = new JLabel("");
                enemy_name_lbl[i] = new JLabel("");
                setLabelFont(enemy_hp_lbl[i], Color.WHITE, 40, 50, 300, 25, 20, false);
                setLabelFont(enemy_name_lbl[i], Color.WHITE, 40, 50, 300, 25, 20, false);
                status_M[i].revalidate();
                status_S[i].revalidate();
            }

            btn = new JButton[8];
            btn2 = new JButton[8];
            for (int i = 0; i < 8; i++) {
                btn[i] = new JButton(Integer.toString(i));
                btn[i].setFont( new Font("游明朝 Regular", Font.PLAIN, 15) );
                btn2[i] = new JButton(Integer.toString(i));
                btn2[i].setFont( new Font("游明朝 Regular", Font.PLAIN, 15) );
                //right_panel.add(btn[i]);
                btn[i].setVisible(false);
                btn2[i].setVisible(false);
                name_lbl[i] = new JLabel("");
                hp_lbl[i] = new JLabel("");
                mp_lbl[i] = new JLabel("");
                lv_lbl[i] = new JLabel("");
            }
            

            disp.repaint();
        }

        //コマンド選択
        void select_command(Game.Character memberS) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    msg_battle.setText(memberS.name+"の行動");
                    right_panel.add(msg_battle);
                    for (int i = 0; i < memberS.skill_num; i++) {
                        btn2[i].setText(memberS.skills[i].name + " (消費MP: " + Integer.toString(memberS.skills[i].mp) + ")");
                        
                        int k = i;
                        btn2[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //btn2[k].setEnabled(false);
                                    //buttonClicked = true;
                                    memberS.com=k;
                                    all_button_invisible();
                                    buttonClicked = true;
                                    lock.notify();
                                }
                            }
                        });
                        //btn[k].setEnabled(true);
                        btn2[i].setVisible(true);
                        msg_battle.setVisible(true);
                        right_panel.add(btn2[i]);
                    }
                    right_panel.revalidate();
                    right_panel.repaint();
                    disp.revalidate();
                    disp.repaint();
                }
            });
        }

        //入力部分セット
        void set_inner() {
            inner_Panel.removeAll();
            disp.repaint();
        }

        void set_HP() {
            int roop_count = 4;
            if (P.member < 4) {
                roop_count = P.member;
            }
            enemy_name.removeAll();
            enemy_status.removeAll();
            enemy_name.setLayout(new GridLayout(1,current_battle.alive_enemies));
            enemy_status.setLayout(new GridLayout(1,current_battle.alive_enemies));
            
            for (int i=0; i < roop_count; i++) {
                hp_lbl[i].setText("HP: " + Integer.toString(P.main[i].HP) + " / " + Integer.toString(P.main[i].max_HP));
                mp_lbl[i].setText("MP: " + Integer.toString(P.main[i].MP) + " / " + Integer.toString(P.main[i].max_MP));
                lv_lbl[i].setText("Lv: " + Integer.toString(P.main[i].Lv));
            }
            for (int i = 0; i < current_battle.alive_enemies; i++) {
                enemy_name.add(enemy_name_lbl[i]);
                enemy_status.add(enemy_hp_lbl[i]);
                enemy_name_lbl[i].setText(enemys[i].name);
                enemy_hp_lbl[i].setText("HP: " + Integer.toString(enemys[i].HP) + " / " + Integer.toString(enemys[i].max_HP));
            }
            disp.repaint();

        }
        void set_HP_player() {
            int roop_count = 4;
            if (P.member < 4) {
                roop_count = P.member;
            }
            for (int i=0; i < roop_count; i++) {
                hp_lbl[i].setText("HP: " + Integer.toString(P.main[i].HP) + " / " + Integer.toString(P.main[i].max_HP));
                mp_lbl[i].setText("MP: " + Integer.toString(P.main[i].MP) + " / " + Integer.toString(P.main[i].max_MP));
                lv_lbl[i].setText("Lv: " + Integer.toString(P.main[i].Lv));
            }
            disp.revalidate();
            disp.repaint();

        }
        //敵のセット
        void set_enemy(int enemy_num) {
            enemy_name.removeAll();
            enemy_status.removeAll();
            enemy_name.setLayout(new GridLayout(1,enemy_num));
            enemy_status.setLayout(new GridLayout(1,enemy_num));
            for (int i = 0; i < enemy_num; i++) {
                enemy_name_lbl[i].setText(enemys[i].name);
                enemy_hp_lbl[i].setText("HP: " + Integer.toString(enemys[i].HP) + " / " + Integer.toString(enemys[i].max_HP));
                enemy_name.add(enemy_name_lbl[i],BorderLayout.CENTER);
                enemy_status.add(enemy_hp_lbl[i],BorderLayout.CENTER);
            }
            disp.repaint();
        }

        //対象の選択
        void set_select_button(String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    msg_battle.setText(text);
                    right_panel.revalidate();
                    right_panel.add(msg_battle);
                    for (int i = 0; i < current_battle.alive_enemies; i++) {
                        final int k = i;
                        btn[i].setText("("+Integer.toString(i+1) +") "+ enemys[i].name);
                        
                        btn[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    enemy_selected = k;
                                    all_button_invisible();
                                    buttonClicked = true;
                                    lock.notify();
                                }
                            }
                        });
                        btn[i].setVisible(true);
                        right_panel.add(btn[i]);
                    }
                    right_panel.revalidate();
                    right_panel.repaint();
                    disp.revalidate();
                    disp.repaint();
                }
            });
        }
        
        void select_friendly(String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText(text);
                    right_panel.add(msg_battle);
                    int roop_count = 4;
                    if (P.member < 4) {
                        roop_count = P.member;
                    }
                    for (int i = 0; i < roop_count; i++) {
                        final int k = i;
                        btn[i].setText("("+Integer.toString(i+1) +") "+ P.main[i].name);
                        btn[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //sbuttonClicked = true;
                                    enemy_selected = k;
                                    all_button_invisible();
                                    buttonClicked = true;
                                    lock.notify();
                                }
                            }
                        });
                        btn[i].setVisible(true);
                        
                        right_panel.add(btn[i]);

                    }
                    msg_battle.setVisible(true);
                    right_panel.revalidate();
                    right_panel.repaint();
                    disp.revalidate();
                    disp.repaint();
                }
            });
        
        }

        void select_allE(String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText(text);
                    right_panel.add(msg_battle);
        
                    btn[0].setText("敵全体");
                    btn[0].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                all_button_invisible();
                                buttonClicked = true;
                                lock.notify();
                            }
                        }
                    });
                    btn[0].setVisible(true);
                    right_panel.add(btn[0]);
                    msg_battle.setVisible(true);
                    right_panel.repaint();
                }
            });
        }

        void select_allF(String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText(text);
                    right_panel.add(msg_battle);
        
                    btn[0].setText("味方全体");
                    btn[0].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                all_button_invisible();
                                buttonClicked = true;
                                lock.notify();
                            }
                        }
                    });
                    btn[0].setVisible(true);
                    right_panel.add(btn[0]);
                    msg_battle.setVisible(true);
                    right_panel.repaint();
                }
            });
        }

        void select_revive(String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText(text);
                    right_panel.add(msg_battle);
                    for (int i = 0; i < 4; i++) {
                        if (P.main[i].death == 1) {
                            final int k = i;
                            btn[i].setText("("+Integer.toString(i+1) +") "+ P.main[i].name);
                            btn[i].addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    synchronized (lock) {
                                        //sbuttonClicked = true;
                                        enemy_selected = k;
                                        //all_button_invisible();
                                        buttonClicked = true;
                                        lock.notify();
                                    }
                                }
                            });
                            btn[i].setVisible(true);
                            
                            right_panel.add(btn[i]);
                        }
                    }
                    msg_battle.setVisible(true);
                    right_panel.revalidate();
                    right_panel.repaint();
                    disp.revalidate();
                    disp.repaint();
                }
            });
        }

        void change_member_main() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText("メイン");
                    right_panel.add(msg_battle);
                    for (int i = 0; i < 4; i++) {
                        final int k = i;
                        btn[i].setText("("+Integer.toString(i+1) +") "+ P.main[i].name);
                        btn[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //sbuttonClicked = true;
                                    stage_selected = k;
                                    all_button_invisible();
                                    buttonClicked = true;
                                    lock.notify();
                                }
                            }
                        });
                        btn[i].setVisible(true);
                        
                        right_panel.add(btn[i]);

                    }
                    msg_battle.setVisible(true);
                    right_panel.revalidate();
                    right_panel.repaint();
                    disp.revalidate();
                    disp.repaint();
                }
            });
        }

        void change_member_sub() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText("控え");
                    right_panel.add(msg_battle);
                    int roop_count = P.member - 4;
                    for (int i = 0; i < roop_count; i++) {
                        final int k = i;
                        btn[i].setText("("+Integer.toString(i+1) +") "+ P.sub[i].name);
                        btn[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //sbuttonClicked = true;
                                    field_selected = k;
                                    all_button_invisible();
                                    buttonClicked = true;
                                    lock.notify();
                                }
                            }
                        });
                        btn[i].setVisible(true);
                        
                        right_panel.add(btn[i]);

                    }
                    msg_battle.setVisible(true);
                    right_panel.revalidate();
                    right_panel.repaint();
                    disp.revalidate();
                    disp.repaint();
                }
            });
        }

        void change_member_name() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText("");
                    right_panel.add(msg_battle);
                    stage_selected = -1;
                    field_selected = -1;
                    int roop_count = 4;
                    if (P.member < 4) {
                        roop_count = P.member;
                    }
                    for (int i = 0; i < roop_count; i++) {
                        final int k = i;
                        btn[i].setText("("+Integer.toString(i+1) +") "+ P.main[i].name);
                        btn[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //sbuttonClicked = true;
                                    stage_selected = k;
                                    all_button_invisible();
                                    buttonClicked = true;
                                    lock.notify();
                                }
                            }
                        });
                        btn[i].setVisible(true);
                        
                        right_panel.add(btn[i]);
                    }

                    for (int i = 0; i < P.member-4; i++) {
                        final int k = i;
                        btn2[i].setText("("+Integer.toString(i+1) +") "+ P.sub[i].name);
                        btn2[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //sbuttonClicked = true;
                                    field_selected = k;
                                    all_button_invisible();
                                    buttonClicked = true;
                                    lock.notify();
                                }
                            }
                        });
                        btn2[i].setVisible(true);
                        
                        right_panel.add(btn2[i]);
                    }
                    msg_battle.setVisible(true);
                    right_panel.revalidate();
                    right_panel.repaint();
                    disp.revalidate();
                    disp.repaint();
                }
            });
        }

        void change_name() {
            
            JTextField textField = new JTextField(20);
            JButton submitButton = new JButton("入力");
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    synchronized (lock) {
                        inputText = textField.getText();
                        buttonClicked = true;
                        set_inner();
                        lock.notify();
                    }
                    
                }
            });
            textField.setColumns(10);
            textField.setFont(new Font("游明朝 Regular", Font.PLAIN, 50));
            inner_Panel.setLayout(new BorderLayout());
            inner_Panel.add(textField,BorderLayout.CENTER);
            inner_Panel.add(submitButton,BorderLayout.EAST);
            inner_Panel.revalidate();
            inner_Panel.repaint();
            disp.revalidate();
            disp.repaint();
        
        }
        //コメントの表示
        public void comment(String line) {
            
            String[] splitline = splitString(line, 40);

            for (String s : splitline) {
                msg_lbl1.setText(msg_lbl2.getText());
                msg_lbl2.setText(msg_lbl3.getText());
                msg_lbl3.setText(msg_lbl4.getText());
                msg_lbl4.setText(s);
                setLabelFont(msg_lbl1, Color.WHITE, 10, 10, 600, 25, 20, false);
                setLabelFont(msg_lbl2, Color.WHITE, 10, 10, 600, 25, 20, false);
                setLabelFont(msg_lbl3, Color.WHITE, 10, 10, 600, 25, 20, false);
                setLabelFont(msg_lbl4, Color.WHITE, 10, 10, 600, 25, 20, false);
                try {
                    Thread.sleep(650);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            disp.repaint();
        }
        
        void all_button_invisible() {
            right_panel.removeAll();
            right_panel.revalidate();
            right_panel.repaint();
            disp.add( right_panel, BorderLayout.EAST);
            disp.revalidate();
            disp.repaint();
            for (int i = 0; i < 8; i++) {
                for (ActionListener actionListener : btn[i].getActionListeners()) {
                    btn[i].removeActionListener(actionListener);
                }
                for (ActionListener actionListener : btn2[i].getActionListeners()) {
                    btn2[i].removeActionListener(actionListener);
                }
            }
        }
        
        void move_bottunS(String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText(text);
                    right_panel.add(msg_battle);
                    for (int i = 0; i < stage_num; i++) {
                        final int k = i;
                        btn[i].setText("("+Integer.toString(i+1) +") "+ stages[i].name);
                        btn[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //sbuttonClicked = true;
                                    stage_selected = k;
                                    //all_button_invisible();
                                    buttonClicked = true;
                                    lock.notify();
                                }
                            }
                        });
                        btn[i].setVisible(true);
                        
                        right_panel.add(btn[i]);

                    }
                    msg_battle.setVisible(true);
                    right_panel.revalidate();
                    right_panel.repaint();
                    disp.revalidate();
                    disp.repaint();
                }
            });
        }

        void move_bottunF(String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText(text);
                    right_panel.add(msg_battle);
                    for (int i = 0; i < stages[current_stage].field_count; i++) {
                        final int k = i;
                        btn2[i].setText("("+Integer.toString(i+1) +") "+ stages[current_stage].fields[i].name);
                        btn2[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //buttonClicked2 = true;
                                    field_selected = k;
                                    //all_button_invisible();
                                    buttonClicked = true;
                                    lock.notify();
                                }
                            }
                        });
                        btn2[i].setVisible(true);
                        right_panel.add(btn2[i]);

                    }
                    msg_battle.setVisible(true);
                    right_panel.revalidate();
                    right_panel.repaint();
                    disp.revalidate();
                    disp.repaint();
                }
            });
        
        }
        
        void yes_no(String text) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText(text);
                    right_panel.add(msg_battle);
        
                    btn[0].setText("はい");
                    btn[0].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                //buttonClicked = true;
                                yes_no_selected = 1;
                                all_button_invisible();
                                buttonClicked = true;
                                lock.notify();
                            }
                        }
                    });
                    btn[0].setVisible(true);
                    right_panel.add(btn[0]);
        
                    btn[1].setText("いいえ");
                    btn[1].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                //buttonClicked = true;
                                yes_no_selected = 0;
                                all_button_invisible();
                                buttonClicked = true;
                                lock.notify();
                            }
                        }
                    });
                    btn[1].setVisible(true);
                    right_panel.add(btn[1]);
                    msg_battle.setVisible(true);
        
                    //disp.repaint();
                    right_panel.repaint();
                }
            });
        }

        void move1() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText("");
                    right_panel.add(msg_battle);
        
                    btn[0].setText("たたかう");
                    btn[0].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                //buttonClicked3 = true;
                                command_selected = 1;
                                buttonClicked = true;
                                all_button_invisible();
                                lock.notify();
                            }
                        }
                    });
                    btn[0].setVisible(true);
                    right_panel.add(btn[0]);
        
                    btn[1].setText("にげる");
                    btn[1].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                //buttonClicked3 = true;
                                command_selected = 2;
                                buttonClicked = true;
                                all_button_invisible();
                                lock.notify();
                            }
                        }
                    });
                    btn[1].setVisible(true);
                    right_panel.add(btn[1]);
                    msg_battle.setVisible(true);
                    if (P.member > 4) {
                        btn[2].setText("いれかえ");
                        btn[2].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //buttonClicked3 = true;
                                    command_selected = 3;
                                    buttonClicked = true;
                                    all_button_invisible();
                                    lock.notify();
                                }
                            }
                        });
                        btn[2].setVisible(true);
                        right_panel.add(btn[2]);
                    }
                    disp.repaint();
                    right_panel.repaint();
                }
            });

        }
        
        void menu() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public synchronized void run() {
                    right_panel.revalidate();
                    right_panel.setLayout(new GridLayout(9, 1));
                    msg_battle.setText("");
                    right_panel.add(msg_battle);
        
                    btn[0].setText("移動");
                    btn[0].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                //buttonClicked3 = true;
                                command_selected = 1;
                                buttonClicked = true;
                                all_button_invisible();
                                lock.notify();
                            }
                        }
                    });
                    btn[0].setVisible(true);
                    right_panel.add(btn[0]);
        
                    btn[1].setText("ゲームを終了する");
                    btn[1].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                //buttonClicked3 = true;
                                finish();
                            }
                        }
                    });
                    btn[1].setVisible(true);
                    right_panel.add(btn[1]);
                    btn[2].setText("セーブ");
                    btn[2].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                //buttonClicked3 = true;
                                command_selected = 2;
                                buttonClicked = true;
                                all_button_invisible();
                                lock.notify();
                            }
                        }
                    });
                    btn[2].setVisible(true);
                    right_panel.add(btn[2]);
                    msg_battle.setVisible(true);
                    btn[3].setText("名前変更");
                    btn[3].addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            synchronized (lock) {
                                //buttonClicked3 = true;
                                command_selected = 3;
                                buttonClicked = true;
                                all_button_invisible();
                                lock.notify();
                            }
                        }
                    });
                    btn[3].setVisible(true);
                    right_panel.add(btn[3]);
                    if (P.member > 4) {
                        btn[4].setText("入れ替え");
                        btn[4].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //buttonClicked3 = true;
                                    command_selected = 4;
                                    buttonClicked = true;
                                    all_button_invisible();
                                    lock.notify();
                                }
                            }
                        });
                        btn[4].setVisible(true);
                        right_panel.add(btn[4]);
                        btn[5].setText("もどる");
                        btn[5].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //buttonClicked3 = true;
                                    command_selected = 0;
                                    buttonClicked = true;
                                    all_button_invisible();
                                    lock.notify();
                                }
                            }
                        });
                        btn[5].setVisible(true);
                        right_panel.add(btn[5]);
                    } else {
                        btn[4].setText("もどる");
                        btn[4].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                synchronized (lock) {
                                    //buttonClicked3 = true;
                                    command_selected = 0;
                                    buttonClicked = true;
                                    all_button_invisible();
                                    lock.notify();
                                }
                            }
                        });
                        btn[4].setVisible(true);
                        right_panel.add(btn[4]);
                    }
                    disp.repaint();
                    right_panel.repaint();
                }
            });

        }
        
        //終了
        public void finish() {
            disp.setVisible(false);
            disp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            System.exit(0);
        }
        

        public static void setPanel(JPanel panel, Color color, BorderLayout layout, Dimension dimension )
        {
            panel.setBackground(color);        // 背景色を設定
            panel.setLayout(layout);           // レイアウトを設定
            panel.setPreferredSize(dimension); // 表示サイズを設定
            return;
        }

        public static void setLabelFont(JLabel label, Color clr, int x_pos, int y_pos, int x_size, int y_size, int strSize, boolean opq )
        {
            label.setForeground(clr);        // 背景色を設定
            label.setLocation(x_pos, y_pos); // 表示位置を設定
            label.setSize(x_size, y_size);   // 表示サイズを設定
            label.setFont( new Font("游明朝 Regular", Font.PLAIN, strSize) ); // 書式、文字サイズを設定
            label.setHorizontalAlignment(JLabel.CENTER); // 水平方向中央揃え
            label.setVerticalAlignment(JLabel.CENTER);   // 垂直方向中央揃え
            label.setOpaque(opq); // ラベルの透明性を設定(true＝不透明、false＝透明)

            return;
        }

        private static String[] splitString(String input, int chunkSize) {
            int length = input.length();
            int numOfChunks = (int) Math.ceil((double) length / chunkSize);
            String[] result = new String[numOfChunks];

            for (int i = 0; i < numOfChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min((i + 1) * chunkSize, length);
                result[i] = input.substring(start, end);
            }

            return result;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            

        }

        // ボタンの設定
        public static void setButton(JButton btn, ActionListener al, int x_size, int y_size, int strSize )
        {
            btn.setPreferredSize(new Dimension(x_size, y_size));      // 表示サイズを設定
            btn.setFont( new Font("游明朝 Regular", Font.PLAIN, strSize) ); // 書式、文字サイズを設定
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.WHITE);
            btn.addActionListener(al); // ボタンが押された時のイベントを受け取れるように設定
            return;
        }
    }

    class Speed implements Serializable {
        int speed;
        int order;
    }

    abstract class Character implements Serializable {
        String name;
        int EXP;
        int Lv;
        int max_HP;
        int max_MP;
        int org_atk;
        int org_matk;
        int org_dif;
        int org_spd;
        int real_atk;
        int real_matk;
        int real_dif;
        int real_spd;
        int HP;
        int MP;
        int regene;
        int sleep;
        int poison;
        int death;
        volatile int com;
        int order;
        Skill[] skills;
        int skill_num;
    
        //レベルアップ時のステータスアップ
        abstract void statusup();
    
        //経験値の取得
        void getEXP(int exp) {
            int k;
            D.comment(name + "は" + Integer.toString(exp) + "経験値を取得");
            k = EXP - exp;
            while (true) {
                if (k < 1)
                    {
                        Lv = Lv + 1;
                        D.comment(name + "はレベルアップした！");
                        statusup();
                        EXP = 30 + (int)Math.exp((double)Lv / 15) + 15 * Lv + Lv * Lv + k;
                        k = EXP;
                    }
                        else
                    {
                        EXP = k;
                    }
                if (EXP > 0) {
                    break;
                }
            }
            D.comment(name + "は次のレベルまで" + Integer.toString(EXP) + "経験値");
        }
    
        void rest() {
            HP = max_HP;
            MP = max_MP;
            death = 0;
        }
    
        void buffreset() {
            real_atk = org_atk;
            real_dif = org_dif;
            real_matk = org_matk;
            real_spd = org_spd;
            regene = 0;
            sleep = 0;
            poison = 0;
        }
    
        void get_skill(Skill new_skill) {
            Skill[] newSkills = Arrays.copyOf(skills, skill_num + 1);
            newSkills[skill_num] = new_skill;
            newSkills[skill_num].slot = skill_num;
            newSkills[skill_num].user = this;
            skills = newSkills;
            skill_num++;
        }

        void get_first_skill(Skill new_skill) {
            skills = new Skill[1];
            skills[skill_num] = new_skill;
            skills[skill_num].slot = skill_num;
            skills[skill_num].user = this;
            skill_num++;
        }

        void heal(int heal_hp) {
            if (death == 0) {
                if(HP + heal_hp < max_HP) {
                    HP += heal_hp;
                } else {
                    heal_hp = max_HP - HP;
                    HP = max_HP;
                }
                D.comment(name+"は"+Integer.toString(heal_hp)+"回復した．");
            }
        }

    }
    
    class Enemy extends Character{
        int plus_hp;
        int plus_atk;
        int plus_dif;
        int plus_matk;
        int plus_spd;
    
        Enemy(String str,int Lv,int max_HP,int max_MP,int atk,int matk,int dif,int spd,int plus_hp, int plus_atk, int plus_dif, int plus_matk, int plus_spd) {
            this.name = str;
            this.Lv = Lv;
            this.max_HP = max_HP;
            this.max_MP = max_MP;
            this.org_atk = atk;
            this.org_matk = matk;
            this.org_dif = dif;
            this.org_spd = spd;
            this.plus_hp = plus_hp;
            this.plus_atk = plus_atk;
            this.plus_dif = plus_dif;
            this.plus_matk = plus_matk;
            this.plus_spd = plus_spd;
            this.real_atk = atk;
            this.real_matk = matk;
            this.real_dif = dif;
            this.real_spd = spd;
            this.com = 1;
            this.regene = 0;
            this.sleep = 0;
            this.poison = 0;
            this.death = 0;
            this.order = 10;
            this.skill_num = 0;
        }

        void status_set() {
            max_HP += rand(plus_hp);
            org_atk += rand(plus_atk);
            org_matk += rand(plus_matk);
            org_dif += rand(plus_dif);
            org_spd += rand(plus_spd);
            real_atk = org_atk;
            real_dif = org_dif;
            real_matk = org_matk;
            real_spd = org_spd;
            HP = max_HP;
            MP = max_MP;
        }

        void cul_EXP() {
            this.EXP = max_HP + org_atk;
        }
        void statusup(){}
        
        public Enemy(Enemy other) {
            this.name = other.name;
            this.EXP = other.EXP;
            this.Lv = other.Lv;
            this.max_HP = other.max_HP;
            this.max_MP = other.max_MP;
            this.org_atk = other.org_atk;
            this.org_matk = other.org_matk;
            this.org_dif = other.org_dif;
            this.org_spd = other.org_spd;
            this.real_atk = other.real_atk;
            this.real_matk = other.real_matk;
            this.real_dif = other.real_dif;
            this.real_spd = other.real_spd;
            this.HP = other.HP;
            this.regene = other.regene;
            this.sleep = other.sleep;
            this.poison = other.poison;
            this.death = other.death;
            this.com = other.com;
            this.order = other.order;
            this.skills = other.skills;
            this.skill_num = other.skill_num;
            for (int i = 0; i<this.skill_num;i++) {
                this.skills[i].user = this;
            }
            this.plus_hp = other.plus_hp;
            this.plus_atk = other.plus_atk;
            this.plus_dif = other.plus_dif;
            this.plus_matk = other.plus_matk;
            this.plus_spd = other.plus_spd;
        }
    }
    
    public class Party implements Serializable {
        Character[] main = new Character[4];
        Character[] sub = new Character[4];
        int main_member;
        int member;
        int alive_player;
    
        Party(Character hero) {
            this.main[0] = hero;
            this.member = 1;
            this.main_member = 1;
            this.alive_player = 1;
        }
    
        void rest() {
            if (member<5) {
                for (int i = 0; i < member; i++)
                {
                    main[i].rest();
                }
            } else {
                for (int i = 0; i < 4; i++)
                {
                    main[i].rest();
                }
                for (int i = 0; i < member-4; i++)
                {
                    sub[i].rest();
                }
            }
            alive_player = member;
        }
    
        void buffreset() {
            if (member<5) {
                for (int i = 0; i < member; i++)
                {
                    main[i].buffreset();
                }
            } else {
                for (int i = 0; i < 4; i++)
                {
                    main[i].buffreset();
                }
                for (int i = 0; i < member-4; i++)
                {
                    sub[i].buffreset();
                }
            }
        }
    
        void add(Character new_member) {
            if (member<4) {
                main[member] = new_member;
                main_member = main_member + 1;
            } else {
                sub[member-4] = new_member;
            }
            alive_player = alive_player + 1;
            member = member + 1;
        }
    
        void getEXP(int exp_got) {
            if (member<5) {
                for (int i = 0; i < member; i++)
                {
                    main[i].getEXP(exp_got);
                }
            } else {
                for (int i = 0; i < 4; i++)
                {
                    main[i].getEXP(exp_got);
                }
            }
            D.set_HP_player();
        }
    
        void change_name() {
            D.comment("誰の名前を変更する？");
            D.change_member_name();
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            D.change_name();
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            if (stage_selected != -1) {
                main[stage_selected].name = inputText;
            } else {
                sub[field_selected].name = inputText;
            }
            D.status_main();
        }

        void change() {
            D.comment("誰を入れ替える？");
            D.change_member_main();
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            D.status_sub();
            D.change_member_sub();
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            Character x = main[stage_selected];
            main[stage_selected] = sub[field_selected];
            sub[field_selected] = x;
            D.status_main();
        }
    }
    
    abstract class Skill implements Serializable {
        String name;
        Character user;
        int slot;
        int mp;
        abstract void select();
        void less_mp(){
            D.comment(user.name+"は魔力が足りない！");
        }
        abstract void act();
    }

    abstract class Self extends Skill {
        void act(){
            if (user.MP >= mp) {
                buff();
                user.MP -= mp;
            } else {
                less_mp();
            }
        }
        void select(){}
        abstract void buff();
    }

    class HealSelf extends Self {
        HealSelf() {
            this.name = "自己再生";
            this.mp = 20;
        }

        void buff () {
            int d = user.real_matk/2 + rand(user.Lv);
            D.comment(user.name+"の自己再生！");
            user.regene = d;
        }

    }

    abstract class ToE extends Skill {
        Enemy target;
        void act(){
            if (target.death==1) {
                target = enemys[0];
            }
            if (user.MP >= mp) {
                damage();
                user.MP -= mp;
            } else {
                less_mp();
            }
        }
        void select(){
            selectE();
        }

        abstract void selectE();
        abstract void damage();
    }

    class Attack extends ToE {
        Attack() {
            this.name = "攻撃";
            this. mp = 0;
        }

        void damage(){
            int d = user.real_atk / 2 - target.real_dif / 4 + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の攻撃" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class Assassin extends ToE {
        Assassin() {
            this.name = "暗剣";
            this. mp = 15;
        }

        void damage(){
            int d = user.real_atk + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の暗剣！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class FireAttack extends ToE {
        FireAttack() {
            this.name = "火炎斬り";
            this.mp = 5;
        }
        
        void damage(){
            int d = (user.real_atk+user.real_matk) / 2 - target.real_dif / 4 + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の火炎斬り" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }

    class FireAttack2 extends ToE {
        FireAttack2() {
            this.name = "マグマ斬り";
            this.mp = 20;
        }
        
        void damage(){
            int d = (user.real_atk+user.real_matk) / 2 - target.real_dif / 4 + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            d = (int)(d * 2.3);
            D.comment(user.name + "の火炎斬り" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class DragonAttack extends ToE {
        DragonAttack() {
            this.name = "竜撃";
            this.mp = 8;
        }
        
        void damage(){
            int d = (user.real_atk+user.real_matk) - target.real_dif / 4 + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の竜撃！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class DragonAttack2 extends ToE {
        DragonAttack2() {
            this.name = "竜撃+";
            this.mp = 15;
        }
        
        void damage(){
            int d = (int)(2.2*(user.real_atk+user.real_matk)) - target.real_dif / 4 + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の竜撃！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class ThreeAttack extends ToE {
        ThreeAttack() {
            this.name = "三段斬り";
            this.mp = 8;
        }
        
        void damage(){
            D.comment(user.name + "の三段斬り");
            for (int i = 0;i<3;i++) {
                int d = (user.real_atk+rand(40))*4 / 10 - target.real_dif / 4;
                if (d < 0) {
                    d = 1;
                }
                D.comment(Integer.toString(d) + "のダメージ");
                target.HP -= d;
            }
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class FourAttack extends ToE {
        FourAttack() {
            this.name = "四段斬り";
            this.mp = 15;
        }
        
        void damage(){
            D.comment(user.name + "の三段斬り");
            for (int i = 0;i<4;i++) {
                int d = (user.real_atk+rand(40))*6 / 10 - target.real_dif / 4;
                if (d < 0) {
                    d = 1;
                }
                D.comment(Integer.toString(d) + "のダメージ");
                target.HP -= d;
            }
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class HealAttack extends ToE {
        HealAttack() {
            this.name = "奇跡の剣";
            this.mp = 13;
        }
        
        void damage(){
            int d = (int)(1.5 * user.real_atk) + user.Lv + rand(20) - target.real_dif / 4;
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の奇跡の剣！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
            d /= 5;
            user.heal(d);
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class HealAttack2 extends ToE {
        HealAttack2() {
            this.name = "奇跡の剣+";
            this.mp = 30;
        }
        
        void damage(){
            int d = (int)(2.3 * user.real_atk) + user.Lv + rand(20) - target.real_dif / 4;
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の奇跡の剣！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
            d /= 3;
            user.heal(d);
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }

    class PoisonAttack extends ToE {
        PoisonAttack() {
            this.name = "毒突き";
            this.mp = 3;
        }
        
        void damage(){
            int d = user.real_atk / 2 + rand(user.Lv) - target.real_dif / 4;
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の毒突き！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
            int poi = rand(10);
            if (poi < 4) {
                target.poison = user.real_atk / 2;
                D.comment(target.name+"は毒にかかった！");
            }
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }

    class PoisonAttack2 extends ToE {
        PoisonAttack2() {
            this.name = "三段毒突き";
            this.mp = 10;
        }
        
        void damage(){
            D.comment(user.name + "の三段毒突き！");
            for (int i=0; i<3;i++) {
                int d = user.real_atk / 2 + rand(user.Lv) - target.real_dif / 4;
                if (d < 0) {
                    d = 1;
                }
                D.comment(Integer.toString(d) + "のダメージ");
                target.HP -= d;
                int poi = rand(10);
                if (poi < 4) {
                    target.poison = 2 * user.real_atk / 3;
                    D.comment(target.name+"は毒にかかった！");
                }
            }
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }

    class PoisonAttack3 extends ToE {
        PoisonAttack3() {
            this.name = "毒うち";
            this.mp = 5;
        }
        
        void damage(){
            int d = user.real_atk / 2 + rand(user.Lv) - target.real_dif / 4;
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の毒うち！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
            int poi = rand(10);
            if (poi < 6) {
                target.poison = user.real_atk / 2;
                D.comment(target.name+"は毒にかかった！");
            }
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }

    class SleepAttack extends ToE {
        SleepAttack() {
            this.name = "眠りうち";
            this.mp = 5;
        }
        
        void damage(){
            int d = user.real_atk / 2 + rand(user.Lv) - target.real_dif / 4;
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "のねむりうち！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
            int slp = rand(10);
            if (slp > 3) {
                D.comment(target.name+"は眠らなかった");
            } else if (slp > 0) {
                target.sleep = 2;
                D.comment(target.name+"は眠った！");
            } else {
                target.sleep = 3;
                D.comment(target.name+"は眠った！");
            }
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }

    class Sleep extends ToE {
        Sleep() {
            this.name = "ねむりうた";
            this.mp = 1;
        }
        
        void damage(){
            D.comment(user.name + "のねむりうた！");
            int slp = rand(10);
            if (slp > 5) {
                D.comment(target.name+"は眠らなかった");
            } else if (slp > 1) {
                target.sleep = 2;
                D.comment(target.name+"は眠った！");
            } else {
                target.sleep = 3;
                D.comment(target.name+"は眠った！");
            }
        }

        void selectE() {
            D.set_select_button("誰を選択する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }

    class Sleep1 extends ToE {
        Sleep1() {
            this.name = "睡魔の術";
            this.mp = 1;
        }
        
        void damage(){
            D.comment(user.name + "の睡魔の術！");
            int slp = rand(10);
            if (slp > 5) {
                D.comment(target.name+"は眠らなかった");
            } else if (slp > 1) {
                target.sleep = 2;
                D.comment(target.name+"は眠った！");
            } else {
                target.sleep = 3;
                D.comment(target.name+"は眠った！");
            }
        }

        void selectE() {
            D.set_select_button("誰を選択する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }

    class Fire extends ToE {
        Fire() {
            this.name = "火遁の術";
            this. mp = 3;
        }

        void damage(){
            int d = user.real_matk + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の火遁の術！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class Fire2 extends ToE {
        Fire2() {
            this.name = "豪火球の術";
            this. mp = 10;
        }

        void damage(){
            int d = user.real_matk + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の豪火球の術！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }
    
    class Fire3 extends ToE {
        Fire3() {
            this.name = "豪火滅却";
            this. mp = 20;
        }

        void damage(){
            int d = 2*user.real_matk + rand(40);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の豪火滅却！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }

    class Thunder extends ToE {
        Thunder() {
            this.name = "稲妻";
            this.mp = 30;
        }
        
        void damage(){
            D.comment(user.name + "の稲妻！");
            int d = (int)(2 * user.real_matk) + user.Lv + rand(20);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の稲妻！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }
    
    class DoubleAttack extends ToE {
        DoubleAttack() {
            this.name = "魔力拳";
            this.mp = 20;
        }
        
        void damage(){
            int d = user.real_atk + rand(user.Lv) - target.real_dif / 4;
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の魔力拳！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
            d = user.real_matk + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            D.comment("追撃！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }

    class BuffAttack extends ToE {
        BuffAttack() {
            this.name = "波動拳";
            this.mp = 30;
        }
        
        void damage(){
            int d = 3*(user.real_atk+user.real_matk)+ rand(user.Lv) - target.real_dif / 4;
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の波動拳！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
            user.real_matk += user.org_matk;
            user.real_dif += user.org_dif/2;
            user.real_atk += user.org_atk / 4;
            D.comment(user.name +"は強化された");
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }

    class BuffAttack2 extends ToE {
        BuffAttack2() {
            this.name = "魔神剣";
            this.mp = 20;
        }
        
        void damage(){
            int d = 2*(user.real_atk+user.real_matk)+ rand(user.Lv) - target.real_dif / 4;
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の魔神拳！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
            user.real_matk += user.org_matk/2;
            user.real_atk += user.org_atk / 4;
            D.comment(user.name +"は強化された");
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
    }

    class Bite extends ToE {
        Bite() {
            this.name = "かみくだく";
            this.mp = 10;
        }
        
        void damage(){
            int d = (int)(1.5*user.real_atk) - target.real_dif / 4 + rand(user.Lv);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "はかみくだいた！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }

        void selectE() {
            D.set_select_button("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = enemys[enemy_selected];
        }
        
    }

    abstract class ToEall extends Skill {
        void act(){
            if (user.MP >= mp) {
                damage();
                user.MP -= mp;
            } else {
                less_mp();
            }
        }
        void select(){
            D.select_allE("誰を攻撃する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }

        }
        abstract void damage();
    }
    
    class Atackall extends ToEall {
        Atackall() {
            this.name = "薙ぎ払い";
            this.mp = 8;
        }

        void damage() {
            D.comment(user.name + "はなぎはらった！");
            for (int i = 0; i < enemy_num; i++) {
                if (enemys[i].death==0) {
                    int d = user.real_atk / 2 - enemys[i].real_dif / 4 + rand(user.Lv);
                    if (d < 0) {
                        d = 1;
                    }
                    D.comment(enemys[i].name+"に"+Integer.toString(d)+"のダメージ");
                    enemys[i].HP -= d;
                }
            }

        }
    }

    class Atackall2 extends ToEall {
        Atackall2() {
            this.name = "ソニックブーム";
            this.mp = 15;
        }

        void damage() {
            D.comment(user.name + "のソニックブーム！");
            for (int i = 0; i < enemy_num; i++) {
                if (enemys[i].death==0) {
                    int d = (user.real_atk+user.real_matk) / 2 + rand(user.Lv);
                    if (d < 0) {
                        d = 1;
                    }
                    D.comment(enemys[i].name+"に"+Integer.toString(d)+"のダメージ");
                    enemys[i].HP -= d;
                }
            }

        }
    }

    class Atackall3 extends ToEall {
        Atackall3() {
            this.name = "天照";
            this.mp = 100;
        }

        void damage() {
            D.comment(user.name + "の天照!！");
            for (int i = 0; i < enemy_num; i++) {
                if (enemys[i].death==0) {
                    int d = 5*user.real_matk + rand(user.Lv);
                    if (d < 0) {
                        d = 1;
                    }
                    D.comment(enemys[i].name+"に"+Integer.toString(d)+"のダメージ");
                    enemys[i].HP -= d;
                }
            }

        }
    }

    class Atackall4 extends ToEall {
        Atackall4() {
            this.name = "闇の波動";
            this.mp = 5;
        }

        void damage() {
            D.comment(user.name + "の闇の波動！");
            for (int i = 0; i < enemy_num; i++) {
                if (enemys[i].death==0) {
                    int d = user.real_matk / 3 + rand(user.Lv);
                    if (d < 0) {
                        d = 1;
                    }
                    D.comment(enemys[i].name+"に"+Integer.toString(d)+"のダメージ");
                    enemys[i].HP -= d;
                }
            }

        }
    }

    abstract class ToF extends Skill {
        Character target;
        void act(){
            if (user.MP >= mp) {
                damage();
                user.MP -= mp;
            } else {
                less_mp();
            }
        }
        void select(){
            selectF();
        }
        abstract void selectF();
        abstract void damage();
    }

    class Heal extends ToF {
        Heal() {
            this.name = "回復+";
            this.mp = 3;
        }

        void damage() {
            int d;
            D.comment(user.name + "の回復！");
            d = rand(20) + user.real_matk;
            target.heal(d);
        }

        void selectF() {
            D.select_friendly("誰を回復する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = P.main[enemy_selected];
        }
    }

    class Heal2 extends ToF {
        Heal2() {
            this.name = "回復++";
            this.mp = 6;
        }

        void damage() {
            int d;
            D.comment(user.name + "の回復！");
            d = rand(40) + 2*user.real_matk;
            target.heal(d);
        }

        void selectF() {
            D.select_friendly("誰を回復する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = P.main[enemy_selected];
        }
    }

    class Healmp extends Self {
        Healmp() {
            this.name = "魔力創造";
            this.mp = 0;
        }

        void buff() {
            int d;
            d = 10 + rand(30) + 3 * user.real_matk;
            if (user.MP + d < user.max_MP)
            {
                user.MP += d;
            }
            else
            {
                d = user.max_MP - user.MP;
                user.MP = user.max_MP;
            }
            D.comment(user.name + "のMPは" + Integer.toString(d) + "回復した");
            
        }

    }

    class Revive extends ToF {
        Revive() {
            this.name = "天使の託宣";
            this.mp = 50;
        }

        void damage() {
            D.comment(user.name + "の天使の託宣！");
            if(target.death==1) {
                D.comment("なんと" + target.name + "は生き返った！");
                target.death = 0;
            }
            int d = user.real_matk + rand(30);
            target.heal(d);
        }

        void selectF() {
            D.select_revive("誰を蘇生する？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            target = P.main[enemy_selected];
        }
    }

    class Buff1 extends Self {
        Buff1() {
            this.name = "森の知恵";
            this.mp = 20;
        }

        void buff() {
            D.comment(user.name + "の森の知恵！");
            user.real_dif += user.org_dif/2;
            user.real_atk += user.org_atk/2;
            D.comment(user.name + "は強化された");
        }
    }

    class Buff2 extends Self {
        Buff2() {
            this.name = "武装";
            this.mp = 15;
        }

        void buff() {
            D.comment(user.name + "の武装！");
            user.real_spd += user.org_spd;
            user.real_atk += user.org_atk;
            D.comment(user.name + "は強化された");
        }
    }

    class Buff3 extends Self {
        Buff3() {
            this.name = "竜の舞";
            this.mp = 20;
        }

        void buff() {
            D.comment(user.name + "の竜の舞！");
            user.real_spd += user.org_spd/2;
            user.real_atk += user.org_atk;
            user.real_dif += user.org_dif;
            user.real_matk += user.real_matk;
            D.comment(user.name + "は強化された");
        }
    }

    class SpeedUp extends ToF {
        SpeedUp() {
            this.name = "俊敏の術";
            this.mp = 20;
        }

        void damage() {
            D.comment(user.name+"の俊敏の術！");
            target.real_spd += target.org_spd;
            D.comment(target.name + "は強化された");
        }

        void selectF() {
            D.select_friendly("誰にかける？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = P.main[enemy_selected];
        }
    }

    class Regene extends ToF {
        Regene() {
            this.name = "癒しうた";
            this.mp = 10;
        }

        void damage() {
            D.comment(user.name+"の癒しうた！");
            int d = user.real_matk + rand(user.Lv);
            target.regene+=d;
            D.comment(user.name+"はターン終了時回復する");
        }

        void selectF() {
            D.select_friendly("誰にかける？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = P.main[enemy_selected];
        }
    }

    class Regene2 extends ToF {
        Regene2() {
            this.name = "癒しうたII";
            this.mp = 20;
        }

        void damage() {
            D.comment(user.name+"の癒しうたII！");
            int d = 2*user.real_matk + rand(user.Lv);
            target.regene+=d;
            D.comment(user.name+"はターン終了時回復する");
        }

        void selectF() {
            D.select_friendly("誰にかける？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = P.main[enemy_selected];
        }
    }

    class Regene3 extends ToF {
        Regene3() {
            this.name = "癒しうたIII";
            this.mp = 30;
        }

        void damage() {
            D.comment(user.name+"の癒しうたIII！");
            int d = 4*user.real_matk + rand(user.Lv);
            target.regene+=d;
            D.comment(user.name+"はターン終了時回復する");
        }

        void selectF() {
            D.select_friendly("誰にかける？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            this.target = P.main[enemy_selected];
        }
    }

    class Difup extends ToFall {
        Difup() {
            this.name = "竜の鱗";
            this.mp = 36;
        }
        void damage() {
            D.comment(user.name + "の竜の鱗！");
            for (int i = 0; i < P.main_member; i ++)
            {
                if (P.main[i].death == 0) {
                    P.main[i].real_dif += 0.6 * P.main[i].org_dif;
                    D.comment(P.main[i].name + "の防御力が上がった！！");
                }
            }
        }
    }

    class Healall extends ToFall {
        Healall() {
            this.name = "天の恵み";
            this.mp = 50;
        }
        void damage() {
            D.comment(user.name + "の天の恵み！");
            for (int i = 0; i < P.main_member; i ++)
            {
                if (P.main[i].death == 0) {
                    int d = user.real_matk + rand(20);
                    P.main[i].heal(d);
                }
            }
        }
    }

    class Healall2 extends ToFall {
        Healall2() {
            this.name = "天の恵み+";
            this.mp = 75;
        }
        void damage() {
            D.comment(user.name + "の天の恵み！");
            for (int i = 0; i < P.main_member; i ++)
            {
                if (P.main[i].death == 0) {
                    int d = 2*user.real_matk + rand(50);
                    P.main[i].heal(d);
                }
            }
        }
    }

    class Healall3 extends ToFall {
        Healall3() {
            this.name = "癒しの波動";
            this.mp = 40;
        }
        void damage() {
            D.comment(user.name + "の癒しの波動！");
            for (int i = 0; i < P.main_member; i ++)
            {
                if (P.main[i].death == 0) {
                    int d = (int)(0.8*user.real_matk) + rand(50);
                    P.main[i].heal(d);
                }
            }
        }
    }

    class Inspire extends ToFall {
        Inspire() {
            this.name = "鼓舞";
            this.mp = 30;
        }
        void damage() {
            D.comment(user.name + "は鼓舞した！");
            for (int i = 0; i < P.main_member; i ++)
            {
                if (P.main[i].death == 0) {
                    P.main[i].real_atk += 0.6 * P.main[i].org_atk;
                    D.comment(P.main[i].name + "の攻撃力が上がった！！");
                }
            }
        }
    }
    
    abstract class ToFall extends Skill {
        void act(){
            if (user.MP >= mp) {
                damage();
                user.MP -= mp;
            } else {
                less_mp();
            }
        }
        void select(){
            D.select_allF("");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }

        }
        abstract void damage();
    }
    
    abstract class EtoF extends Skill {
        Character target;
        void act(){
            if (user.MP >= mp) {
                damage();
                user.MP -= mp;
            } else {
                less_mp();
            }
        }
        void select() {
            int target_num;
            target_num = rand(P.main_member);
            while (true)
            {
                if (P.main[target_num].death == 1)
                {
                    if (target_num == P.main_member - 1)
                    {
                        target_num = 0;
                    }
                    else
                    {
                        target_num++;
                    }
                }
                else
                {
                    break;
                }
            }
            target = P.main[target_num];
        }
        abstract void damage();
    }

    class Eattack extends EtoF {
        Eattack() {
            this.name = "攻撃";
            this.mp = 0;
        }

        void damage(){
            int d = user.real_atk / 2 - target.real_dif / 4 + rand(user.Lv);
            if (d < 1) {
                d = 1;
            }
            D.comment(user.name + "の攻撃" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }
    }

    class EHealAttack extends EtoF {
        EHealAttack() {
            this.name = "奇跡の剣";
            this.mp = 13;
        }
        
        void damage(){
            int d = (int)(1.5 * user.real_atk) + user.Lv + rand(20) - target.real_dif / 4;
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の奇跡の剣！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
            d /= 5;
            user.heal(d);
        }
    }
    
    class EThunder extends EtoF {
        EThunder() {
            this.name = "稲妻";
            this.mp = 30;
        }
        void damage(){
            D.comment(user.name + "の稲妻！");
            int d = user.real_matk/2 + user.Lv + rand(20);
            if (d < 0) {
                d = 1;
            }
            D.comment(user.name + "の稲妻！" + Integer.toString(d) + "のダメージ");
            target.HP -= d;
        }
    }

    class Yearn extends EtoF {
        String message;
        Yearn(String message){
            this.mp = 0;
            this.message=message;
        }
        void damage() {
            D.comment(message);
        }
    }

    abstract class EtoFall extends Skill {
        void act(){
            if (user.MP >= mp) {
                damage();
                user.MP -= mp;
            } else {
                less_mp();
            }
        }
        void select(){
        }
        abstract void damage();
    }

    class EAtackall extends EtoFall {
        EAtackall() {
            this.name = "薙ぎ払い";
            this.mp = 8;
        }

        void damage() {
            D.comment(user.name + "はなぎはらった！");
            for (int i = 0; i < P.main_member; i++) {
                if (P.main[i].death==0) {
                    int d = user.real_atk / 2 - P.main[i].real_dif / 4 + rand(user.Lv);
                    if (d < 0) {
                        d = 1;
                    }
                    D.comment(P.main[i].name+"に"+Integer.toString(d)+"のダメージ");
                    P.main[i].HP -= d;
                }
            }

        }
    }

    class Type1 extends Character {
        Type1(String str) {
            this.name = str;
            this.EXP = 20;
            this.Lv = 1;
            this.max_HP = 30;
            this.max_MP = 2;
            this.org_atk = 10;
            this.org_matk = 5;
            this.org_dif = 5;
            this.org_spd = 5;
            this.real_atk = this.org_atk;
            this.real_dif = this.org_dif;
            this.real_matk = this.org_matk;
            this.real_spd = this.org_spd;
            this.com = 1;
            this.regene = 0;
            this.sleep = 0;
            this.poison = 0;
            this.death = 0;
            this.order = 10;
            this.HP = max_HP;
            this.MP = max_MP;
            this.skill_num = 0;
            this.get_first_skill(new Attack());
        }
    
        void statusup() {
            max_HP += 8;
            max_MP += 4;
            org_atk += 4;
            org_matk += 4;
            org_dif += 6;
            org_spd += 4;
            HP = max_HP;
            MP = max_MP;
            add_skill();
        }

        void add_skill () {
            if (Lv == 5) {
                D.comment(name + "は火炎斬りを覚えた！");
                this.get_skill(new FireAttack());
            } else if (Lv == 10) {
                D.comment(name + "は三段斬りを覚えた！");
                this.get_skill(new ThreeAttack());
            } else if (Lv == 18) {
                D.comment(name + "は奇跡の剣を覚えた！");
                this.get_skill(new HealAttack());
            } else if (Lv == 23) {
                D.comment(name + "の火炎斬りはマグマ斬りに強化された！");
                skills[1] = new FireAttack2();
                skills[1].slot = 1;
                skills[1].user = skills[0].user;
            } else if (Lv == 30) {
                D.comment(name + "の三段斬りは四段斬りに強化された！");
                skills[2] = new FourAttack();
                skills[2].slot = 3;
                skills[2].user = skills[0].user;
            } else if (Lv == 40) {
                D.comment(name + "の奇跡の剣は奇跡の剣+に強化された！");
                skills[3] = new HealAttack2();
                skills[3].slot = 3;
                skills[3].user = skills[0].user;
            } else if (Lv == 50) {
                D.comment(name + "は鼓舞を覚えた！");
                this.get_skill(new Inspire());
            }
        }
    }
    
    class Type2 extends Character {
        Type2(String str) {
            this.name = str;
            this.EXP = 180;
            this.Lv = 8;
            this.max_HP = 40;
            this.max_MP = 30;
            this.org_atk = 10;
            this.org_matk = 35;
            this.org_dif = 20;
            this.org_spd = 16;
            this.real_atk = this.org_atk;
            this.real_dif = this.org_dif;
            this.real_matk = this.org_matk;
            this.real_spd = this.org_spd;
            this.com = 1;
            this.regene = 0;
            this.sleep = 0;
            this.poison = 0;
            this.death = 0;
            this.order = 10;
            this.HP = max_HP;
            this.MP = max_MP;
            this.skill_num = 0;
            this.get_first_skill(new Attack());
            this.get_skill(new Heal());
        }
    
        void statusup() {
            max_HP += 6;
            max_MP += 6;
            org_atk += 2;
            org_matk += 5;
            org_dif += 7;
            org_spd += 4;
            HP = max_HP;
            MP = max_MP;
            add_skill();
        }
    
        void add_skill () {
            if (Lv == 10) {
                D.comment(name + "は稲妻を覚えた！");
                this.get_skill(new Thunder());
            } else if (Lv == 18) {
                D.comment(name + "は魔力創造を覚えた！");
                this.get_skill(new Healmp());
            } else if (Lv == 25) {
                D.comment(name + "は天の恵みを覚えた！");
                this.get_skill(new Healall());
            } else if (Lv == 30) {
                D.comment(name + "の回復+は回復++に強化された！");
                skills[1] = new Heal2();
                skills[1].slot = 1;
                skills[1].user = skills[0].user;
            } else if (Lv == 50) {
                D.comment(name + "は天使の託宣を覚えた！");
                this.get_skill(new Revive());
            } else if (Lv == 55) {
                D.comment(name + "の天の恵みは天の恵み+に強化された！");
                skills[4] = new Healall2();
                skills[4].slot = 4;
                skills[4].user = skills[0].user;
            }
        }
    }
    
    class Type3 extends Character {
        Type3(String str) {
            this.name = str;
            this.EXP = 240;
            this.Lv = 7;
            this.max_HP = 50;
            this.max_MP = 25;
            this.org_atk = 40;
            this.org_matk = 15;
            this.org_dif = 30;
            this.org_spd = 40;
            this.real_atk = this.org_atk;
            this.real_dif = this.org_dif;
            this.real_matk = this.org_matk;
            this.real_spd = this.org_spd;
            this.com = 1;
            this.regene = 0;
            this.sleep = 0;
            this.poison = 0;
            this.death = 0;
            this.order = 10;
            this.HP = max_HP;
            this.MP = max_MP;
            this.skill_num = 0;
            this.get_first_skill(new Attack());
            this.get_skill(new PoisonAttack());
        }
    
        void statusup() {
            max_HP += 7;
            max_MP += 3;
            org_atk += 5;
            org_matk += 2;
            org_dif += 8;
            org_spd += 5;
            HP = max_HP;
            MP = max_MP;
            add_skill();
        }

        void add_skill () {
            if (Lv == 10) {
                D.comment(name + "はなぎはらいを覚えた！");
                this.get_skill(new Atackall());
            } else if (Lv == 20) {
                D.comment(name + "は武装を覚えた！");
                this.get_skill(new Buff2());
            } else if (Lv == 23) {
                D.comment(name + "の毒突きは三段毒突きに強化された！");
                skills[1] = new PoisonAttack2();
                skills[1].slot = 1;
                skills[1].user = skills[0].user;
            } else if (Lv == 35) {
                D.comment(name + "は魔力拳を覚えた！");
                this.get_skill(new DoubleAttack());
            } else if (Lv == 50) {
                D.comment(name + "は波動拳を覚えた！");
                this.get_skill(new BuffAttack());
            }
        }
    }
    
    class Type4 extends Character {
        Type4(String str) {
            this.name = str;
            this.EXP = 200;
            this.Lv = 20;
            this.max_HP = 120;
            this.max_MP = 40;
            this.org_atk = 80;
            this.org_matk = 60;
            this.org_dif = 80;
            this.org_spd = 50;
            this.real_atk = this.org_atk;
            this.real_dif = this.org_dif;
            this.real_matk = this.org_matk;
            this.real_spd = this.org_spd;
            this.com = 1;
            this.regene = 0;
            this.sleep = 0;
            this.poison = 0;
            this.death = 0;
            this.order = 10;
            this.HP = max_HP;
            this.MP = max_MP;
            this.skill_num = 0;
            this.get_first_skill(new Attack());
            this.get_skill(new Bite());
        }
    
        void statusup() {
            max_HP += 8;
            max_MP += 4;
            org_atk += 5;
            org_matk += 3;
            org_dif += 9;
            org_spd += 4;
            HP = max_HP;
            MP = max_MP;
            add_skill();
        }

        void add_skill () {
            if (Lv == 23) {
                D.comment(name + "は自己再生を覚えた！");
                this.get_skill(new HealSelf());
            } else if (Lv == 30) {
                D.comment(name + "は竜の鱗を覚えた！");
                this.get_skill(new Difup());
            } else if (Lv == 50) {
                D.comment(name + "は竜の舞を覚えた！");
                this.get_skill(new Buff3());
            } else if (Lv == 60) {
                D.comment(name + "は竜撃を覚えた！");
                this.get_skill(new DragonAttack());
            } else if (Lv == 75) {
                D.comment(name + "の竜撃は竜撃+に強化された！");
                skills[5] = new DragonAttack2();
                skills[5].slot = 5;
                skills[5].user = skills[0].user;
            }
        }
    
    }
    
    class Type5 extends Character {
        Type5(String str) {
            this.name = str;
            this.EXP = 500;
            this.Lv = 20;
            this.max_HP = 90;
            this.max_MP = 50;
            this.org_atk = 70;
            this.org_matk = 70;
            this.org_dif = 70;
            this.org_spd = 80;
            this.real_atk = this.org_atk;
            this.real_dif = this.org_dif;
            this.real_matk = this.org_matk;
            this.real_spd = this.org_spd;
            this.com = 1;
            this.regene = 0;
            this.sleep = 0;
            this.poison = 0;
            this.death = 0;
            this.order = 10;
            this.HP = max_HP;
            this.MP = max_MP;
            this.skill_num = 0;
            this.get_first_skill(new Attack());
            this.get_skill(new SleepAttack());
            this.get_skill(new PoisonAttack3());
            this.get_skill(new Heal());

        }
    
        void statusup() {
            max_HP += 7;
            max_MP += 5;
            org_atk += 5;
            org_matk += 5;
            org_dif += 5;
            org_spd += 7;
            HP = max_HP;
            MP = max_MP;
            add_skill();
        }

        void add_skill () {
            if (Lv == 30) {
                D.comment(name + "の回復+は回復++に強化された！");
                skills[3]=new Heal2();
                skills[3].slot = 3;
                skills[3].user = skills[0].user;
            } else if (Lv == 40) {
                D.comment(name + "は暗剣を覚えた！");
                this.get_skill(new Assassin());
            } else if (Lv == 50) {
                D.comment(name + "は森の知恵を覚えた！");
                this.get_skill(new Buff1());
            }
        }
    }
    
    class Type6 extends Character {
        Type6(String str) {
            this.name = str;
            this.EXP = 500;
            this.Lv = 25;
            this.max_HP = 130;
            this.max_MP = 50;
            this.org_atk = 70;
            this.org_matk = 70;
            this.org_dif = 70;
            this.org_spd = 100;
            this.real_atk = this.org_atk;
            this.real_dif = this.org_dif;
            this.real_matk = this.org_matk;
            this.real_spd = this.org_spd;
            this.com = 1;
            this.regene = 0;
            this.sleep = 0;
            this.poison = 0;
            this.death = 0;
            this.order = 10;
            this.HP = max_HP;
            this.MP = max_MP;
            this.skill_num = 0;
            this.get_first_skill(new Attack());
            this.get_skill(new Sleep());
            this.get_skill(new Healall3());
            this.get_skill(new Regene());
        }
    
        void statusup() {
            max_HP += 7;
            max_MP += 5;
            org_atk += 3;
            org_matk += 5;
            org_dif += 6;
            org_spd += 7;
            HP = max_HP;
            MP = max_MP;
            add_skill();
        }

        void add_skill() {
            if (Lv == 34) {
                D.comment(name + "の癒しうたは癒しうたIIに強化された！");
                skills[3]=new Regene2();
                skills[3].slot = 3;
                skills[3].user = skills[0].user;
            } else if (Lv == 50) {
                D.comment(name + "はソニックブームを覚えた！");
                this.get_skill(new Atackall2());
            } else if (Lv == 70) {
                D.comment(name + "の癒しうたIは癒しうたIIIに強化された！");
                skills[3]=new Regene3();
                skills[3].slot = 3;
                skills[3].user = skills[0].user;
            } else if (Lv == 75) {
                D.comment(name + "は魔力創造を覚えた！");
                this.get_skill(new Healmp());
            }
        }
    }
    
    class Type7 extends Character {
        Type7(String str) {
            this.name = str;
            this.EXP = 5000;
            this.Lv = 40;
            this.max_HP = 250;
            this.max_MP = 200;
            this.org_atk = 70;
            this.org_matk = 200;
            this.org_dif = 130;
            this.org_spd = 150;
            this.real_atk = this.org_atk;
            this.real_dif = this.org_dif;
            this.real_matk = this.org_matk;
            this.real_spd = this.org_spd;
            this.com = 1;
            this.regene = 0;
            this.sleep = 0;
            this.poison = 0;
            this.death = 0;
            this.order = 10;
            this.HP = max_HP;
            this.MP = max_MP;
            this.skill_num = 0;
            this.get_first_skill(new Attack());
            this.get_skill(new Fire());
            this.get_skill(new Sleep1());
            this.get_skill(new SpeedUp());
        }
    
        void statusup() {
            max_HP += 6;
            max_MP += 2;
            org_atk += 2;
            org_matk += 6;
            org_dif += 6;
            org_spd += 8;
            HP = max_HP;
            MP = max_MP;
            add_skill();
        }

        void add_skill() {
            if (Lv == 44) {
                D.comment(name + "の火遁の術は豪火球の術に強化された！");
                skills[1]=new Fire2();
                skills[1].slot = 1;
                skills[1].user = skills[0].user;
            } else if (Lv == 55) {
                D.comment(name + "は暗剣を覚えた！");
                this.get_skill(new Assassin());
            } else if (Lv == 70) {
                D.comment(name + "の豪火球の術は豪火滅却に強化された！");
                skills[1]=new Fire3();
                skills[1].slot = 1;
                skills[1].user = skills[0].user;
            } else if (Lv == 80) {
                D.comment(name + "は天照を覚えた！");
                this.get_skill(new Atackall3());
            }
        }
    }
    
    class Type8 extends Character {
        Type8(String str) {
            this.name = str;
            this.EXP = 18000;
            this.Lv = 60;
            this.max_HP = 540;
            this.max_MP = 300;
            this.org_atk = 450;
            this.org_matk = 450;
            this.org_dif = 400;
            this.org_spd = 300;
            this.real_atk = this.org_atk;
            this.real_dif = this.org_dif;
            this.real_matk = this.org_matk;
            this.real_spd = this.org_spd;
            this.com = 1;
            this.regene = 0;
            this.sleep = 0;
            this.poison = 0;
            this.death = 0;
            this.order = 10;
            this.HP = max_HP;
            this.MP = max_MP;
            this.skill_num = 0;
            this.get_first_skill(new Attack());
            this.get_skill(new Heal());
            this.get_skill(new Atackall4());
            this.get_skill(new HealAttack2());
        }
    
        void statusup() {
            max_HP += 9;
            max_MP += 6;
            org_atk += 6;
            org_matk += 6;
            org_dif += 10;
            org_spd += 9;
            HP = max_HP;
            MP = max_MP;
            add_skill();
        }

        void add_skill() {
            if (Lv == 65) {
                D.comment(name + "は竜撃を覚えた！");
                this.get_skill(new DragonAttack());
            } else if (Lv == 75) {
                D.comment(name + "の竜撃は竜撃+に強化された！");
                skills[4]=new DragonAttack2();
                skills[4].slot = 4;
                skills[4].user = skills[0].user;
            } else if (Lv == 85) {
                D.comment(name + "は魔神剣を覚えた！");
                this.get_skill(new BuffAttack2());
            }
        }
    
    }
    
    class Battle implements Serializable {
        int alive_enemies;
        Battle(int alive_enemies) {
            this.alive_enemies = alive_enemies;
            current_battle = this;
        }
        
        void attack_phase() {
            Speed[] order = new Speed[P.main_member + alive_enemies];
            for (int i = 0; i < P.main_member; i++) {
                P.main[i].order = i;
                order[i] = new Speed();
                order[i].speed = P.main[i].real_spd;
                order[i].order = i;
            }

            for (int i = P.main_member; i < P.main_member + alive_enemies; i++) {
                enemys[i - P.main_member].order = i;
                order[i] = new Speed();
                order[i].speed = enemys[i - P.main_member].real_spd;
                order[i].order = i;
            }

            Arrays.sort(order, Comparator.comparingInt((Speed o) -> o.speed).reversed());

            int turn_alive = P.main_member + alive_enemies;
            for (int j = 0; j < turn_alive; j++) {
                //行動順に沿った行動
                for (int i = 0; i < P.main_member; i++) {
                    if (order[j].order == P.main[i].order) {
                        P.main[i].skills[P.main[i].com].act();
                        P.main[i].order = 10;
                        D.set_HP();
                    }
                }

                for (int k = P.main_member; k < P.main_member + alive_enemies; k++) {
                    if (order[j].order == enemys[k - P.main_member].order) {
                        moveE(enemys[k- P.main_member]);
                        enemys[k - P.main_member].order = 10;
                        D.set_HP();
                    }
                }

                for (int l = 0; l < P.main_member; l++) {
                    if (P.main[l].HP < 1 && P.main[l].death == 0) {
                        P.main[l].death = 1;
                        P.main[l].HP = 0;
                        P.alive_player--;
                    }
                }

                for (int m = P.main_member; m < P.main_member + alive_enemies; m++) {
                    if (enemys[m - P.main_member].HP < 1) {
                        enemys[m - P.main_member].death = 1;
                        for (int s = m - P.main_member; s < alive_enemies - 1; s++) {
                            enemys[s] = enemys[s + 1];
                        }
                        alive_enemies--;
                    }
                }
                if(alive_enemies == 0 || P.alive_player == 0) {
                    D.set_enemy(0);
                    break;
                }
                
            }
        }

        void moveE(Enemy move_enemy){
            if (move_enemy.sleep==0 && move_enemy.death==0) {
                move_enemy.com = rand(move_enemy.skill_num);
                move_enemy.skills[move_enemy.com].select();
                move_enemy.skills[move_enemy.com].act();
            } else {
                D.comment(move_enemy.name + "は眠っている");
            }
        }
        
        int battle_main() {
            int result;
            String names = "";
            for (int i = 0; i < alive_enemies; i++) {
                names += "(" + Integer.toString(i+1) + ") " + enemys[i].name + "  ";
            }
            names += "が現れた!!";
            enemy_num = alive_enemies;
            D.comment(names);
            P.buffreset();
            D.set_enemy(enemy_num);
            
            while (true) {
                int select = 0;
                while (true) {
                    D.move1();
                    try {
                        waitForButtonClick();
                    } catch (InterruptedException e){

                    }
                    select = command_selected;
                    if (select == 3) {
                        P.change();
                    } else {
                        break;
                    }
                }
    
                if (select == 2) {
                    D.set_enemy(0);
                    result = 1;
                    break;
                }
    
                for (int i = 0; i < P.main_member; i++) {
                    command(P.main[i]);
                }
                attack_phase();
                pturnend();
                eturnend();

                for (int l = 0; l < P.main_member; l++) {
                    if (P.main[l].HP < 1 && P.main[l].death == 0) {
                        P.main[l].death = 1;
                        P.main[l].HP = 0;
                        P.alive_player--;
                    }
                }
    
                for (int m = P.main_member; m < P.main_member + enemy_num; m++) {
                    if (enemys[m - P.main_member].HP < 1 && enemys[m - P.main_member].death == 0) {
                        enemys[m - P.main_member].death = 1;
                        for (int s = m - P.main_member; s < enemy_num - 1; s++) {
                            enemys[s] = enemys[s + 1];
                        }
                        alive_enemies--;
                        m--;
                    }
                }
    
                if (alive_enemies == 0) {
                    result = 2;
                    break;
                }
    
                if (P.alive_player == 0) {
                    result = 3;
                    break;
                }
                D.set_HP();

            }
    
            return result;
        }

        void command(Character move_player) {
            if (move_player.death > 0) {
                D.comment(move_player.name + "は行動できない！");
            } else {

                D.select_command(move_player);
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                }
                move_player.skills[move_player.com].select();
            }
        }

        void pturnend() {
            for (int i = 0; i < P.main_member; i++) {
                if (P.main[i].sleep > 0) {
                    P.main[i].sleep--;
                }
    
                if (P.main[i].poison > 0) {
                    P.main[i].HP -= P.main[i].poison;
                    D.comment(P.main[i].name + "は毒で" + Integer.toString(P.main[i].poison) + "ダメージ受けた");
                    P.main[i].poison -= 5 * P.main[i].Lv;
    
                    if (P.main[i].poison < 1) {
                        P.main[i].poison = 0;
                    }
                }
    
                if (P.main[i].regene > 0) {
                    P.main[i].heal(P.main[i].regene);
                    P.main[i].regene -= P.main[i].max_HP / 5;
    
                    if (P.main[i].regene < 1) {
                        P.main[i].regene = 0;
                    }
                }
    
                if (P.main[i].real_atk > P.main[i].org_atk) {
                    P.main[i].real_atk -= P.main[i].org_atk / 8;
    
                    if (P.main[i].real_atk < P.main[i].org_atk) {
                        P.main[i].real_atk = P.main[i].org_atk;
                    }
                }
    
                if (P.main[i].real_matk > P.main[i].org_matk) {
                    P.main[i].real_matk -= P.main[i].org_matk / 8;
    
                    if (P.main[i].real_matk < P.main[i].org_matk) {
                        P.main[i].real_matk = P.main[i].org_matk;
                    }
                }
    
                if (P.main[i].real_dif > P.main[i].org_dif) {
                    P.main[i].real_dif -= P.main[i].org_dif / 8;
    
                    if (P.main[i].real_dif < P.main[i].org_dif) {
                        P.main[i].real_dif = P.main[i].org_dif;
                    }
                }
    
                if (P.main[i].real_spd > P.main[i].org_spd) {
                    P.main[i].real_spd -= P.main[i].org_spd / 8;
    
                    if (P.main[i].real_spd < P.main[i].org_spd) {
                        P.main[i].real_spd = P.main[i].org_spd;
                    }
                }
            }
        }

        void eturnend() {
            for (int i = 0; i < enemy_num; i++) {
                if (enemys[i].sleep > 0) {
                    enemys[i].sleep--;
                }
    
                if (enemys[i].poison > 0) {
                    enemys[i].HP -= enemys[i].poison;
                    D.comment(enemys[i].name + "は毒で" + Integer.toString(enemys[i].poison) + "ダメージ受けた");
                    enemys[i].poison -= 5 * enemys[i].Lv;
    
                    if (enemys[i].poison < 1) {
                        enemys[i].poison = 0;
                    }
                }
    
                if (enemys[i].regene > 0) {
                    enemys[i].heal(enemys[i].regene);
                    enemys[i].regene -= enemys[i].max_HP / 8;
    
                    if (enemys[i].regene < 1) {
                        enemys[i].regene = 0;
                    }
                }
    
                if (enemys[i].real_atk > enemys[i].org_atk) {
                    enemys[i].real_atk -= enemys[i].org_atk / 5;
    
                    if (enemys[i].real_atk < enemys[i].org_atk) {
                        enemys[i].real_atk = enemys[i].org_atk;
                    }
                }
    
                if (enemys[i].real_matk > enemys[i].org_matk) {
                    enemys[i].real_matk -= enemys[i].org_matk / 5;
    
                    if (enemys[i].real_matk < enemys[i].org_matk) {
                        enemys[i].real_matk = enemys[i].org_matk;
                    }
                }
    
                if (enemys[i].real_dif > enemys[i].org_dif) {
                    enemys[i].real_dif -= enemys[i].org_dif / 5;
    
                    if (enemys[i].real_dif < enemys[i].org_dif) {
                        enemys[i].real_dif = enemys[i].org_dif;
                    }
                }
    
                if (enemys[i].real_spd > enemys[i].org_spd) {
                    enemys[i].real_spd -= enemys[i].org_spd / 5;
    
                    if (enemys[i].real_spd < enemys[i].org_spd) {
                        enemys[i].real_spd = enemys[i].org_spd;
                    }
                }
            }
        }
    }
    
    abstract class Event implements Serializable {
        boolean experienced;
        int event_num;
        abstract void execute();
        abstract void triger();
    }

    abstract class AddPlayer extends Event {
        abstract void executeP(String some);
    }

    class addType1 extends AddPlayer {
        addType1() {
            experienced = false;
            event_num = 0;
        }

        void executeP(String name) {
            Game.this.P = new Party(new Type1(name));
            experienced = true;
            D.status_main();
            D.set_inner();
        }
        void execute(){}
        void triger () {
            D.new_member(this);
            D.comment("あなたの名前は？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class addType2 extends AddPlayer {
        addType2() {
            experienced = false;
            event_num = 0;
        }
        
        void executeP(String name) {
            P.add(new Type2(name));
            experienced = true;
            D.status_main();
            D.set_inner();
        }
        void execute(){
            triger();
        }
        void triger () {
            D.comment("囚われていた僧侶が現れた");
            D.comment("僧侶が仲間になった！");
            D.new_member(this);
            D.comment("僧侶の名前は？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class addType3 extends AddPlayer {
        addType3() {
            experienced = false;
            event_num = 0;
        }
        
        void executeP(String name) {
            P.add(new Type3(name));
            experienced = true;
            D.status_main();
            D.set_inner();
        }
        void execute(){
            triger();
        }
        void triger () {
            D.comment("武闘家:魔王を倒すんだって？俺もついていくよ！");
            D.comment("武闘家が仲間になった");
            D.new_member(this);
            D.comment("武闘家の名前は？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class addType4 extends AddPlayer {
        addType4() {
            experienced = false;
            event_num = 0;
        }
        void executeP(String name) {
            P.add(new Type4(name));
            experienced = true;
            D.status_main();
            D.set_inner();
        }
        void execute(){
            triger();
        }
        void triger () {
            D.comment("なんとドラゴンが起き上がった！");
            D.comment("ドラゴンが仲間になった！");
            D.new_member(this);
            D.comment("ドラゴンの名前は？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class addType5 extends AddPlayer {
        addType5() {
            experienced = false;
            event_num = 0;
        }
        void executeP(String name) {
            P.add(new Type5(name));
            experienced = true;
            D.status_main();
            D.set_inner();
        }
        void execute(){
            triger();
        }
        void triger () {
            D.comment("狩人:魔王の影響で森が荒れている...");
            D.comment("狩人:ともに魔王を倒させてくれ！");
            D.comment("狩人が仲間になった！");
            D.new_member(this);
            D.comment("狩人の名前は？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class addType6 extends AddPlayer {
        addType6() {
            experienced = false;
            event_num = 0;
        }
        void executeP(String name) {
            P.add(new Type6(name));
            experienced = true;
            D.status_main();
            D.set_inner();
        }
        void execute(){
            triger();
        }
        void triger () {
            D.comment("囚われていた歌い手が現れた！");
            D.comment("歌い手が仲間になった！");
            D.new_member(this);
            D.comment("歌い手の名前は？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class addType7 extends AddPlayer {
        addType7() {
            experienced = false;
            event_num = 0;
        }
        void executeP(String name) {
            P.add(new Type7(name));
            experienced = true;
            D.status_main();
            D.set_inner();
        }
        void execute(){
            triger();
        }
        void triger () {
            D.comment("忍者:私一人ではここまでが限界だ");
            D.comment("忍者:ともに戦おう");
            D.comment("忍者が仲間になった！");
            D.new_member(this);
            D.comment("忍者の名前は？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    class addType8 extends AddPlayer {
        addType8() {
            experienced = false;
            event_num = 0;
        }
        void executeP(String name) {
            P.add(new Type8(name));
            experienced = true;
            D.status_main();
            D.set_inner();
        }
        void execute(){
            triger();
        }
        void triger () {
            D.comment("魔戦士:私もこの世界を守らねばならない");
            D.comment("魔戦士が仲間になった！");
            D.new_member(this);
            D.comment("魔戦士の名前は？");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class Boss_1 extends Event {
        Boss_1() {
            experienced = false;
            event_num = 0;
        }
        void execute() {
            D.comment("このエリアの主と戦闘しますか？");
            D.yes_no("");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            if (yes_no_selected == 1) {
                triger();
            }
        }
        void triger () {
            enemy_num = 1;
            enemys = new Enemy[1];
            int exp_X = 0;
            enemys[0] = new Enemy("草原の主", 15,200,100,20,40,20,10,20,10,10,10,10);
            enemys[0].status_set();
            enemys[0].cul_EXP();
            enemys[0].get_first_skill(new Eattack());
            enemys[0].get_skill(new Buff1());
            enemys[0].get_skill(new HealSelf());
            exp_X = enemys[0].EXP;
            Battle battle_X = new Battle(enemy_num);
            int result = battle_X.battle_main();
            if (result == 1) {
                D.comment("モンスターから逃げた。");
            }
            else if (result == 2) {
                D.comment("モンスターに勝利した！");
                P.getEXP(exp_X);
                experienced = true;
                stages[1] = new Stage2();
            }
            else if (result == 3) {
                D.comment("GAME OVER");
                D.finish();
            }
        }
    }

    class Boss_2 extends Event {
        Boss_2() {
            experienced = false;
            event_num = 0;
        }
        void execute() {
            D.comment("このエリアの主と戦闘しますか？");
            D.yes_no("");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            if (yes_no_selected == 1) {
                triger();
            }
        }
        void triger () {
            enemy_num = 1;
            enemys = new Enemy[1];
            int exp_X = 0;
            enemys[0] = new Enemy("密林のドラゴン", 25,2000,100,60,40,60,70,20,10,10,10,10);
            enemys[0].status_set();
            enemys[0].cul_EXP();
            enemys[0].get_first_skill(new Eattack());
            enemys[0].get_skill(new Buff3());
            enemys[0].get_skill(new Yearn("密林のドラゴンは欠伸している"));
            exp_X = enemys[0].EXP;
            Battle battle_X = new Battle(enemy_num);
            int result = battle_X.battle_main();
            if (result == 1) {
                D.comment("モンスターから逃げた。");
            }
            else if (result == 2) {
                D.comment("モンスターに勝利した！");
                P.getEXP(exp_X);
                experienced = true;
                stages[2] = new Stage3();
            }
            else if (result == 3) {
                D.comment("GAME OVER");
                D.finish();
            }
        }
    }

    class Boss_3 extends Event {
        Boss_3() {
            experienced = false;
            event_num = 0;
        }
        void execute() {
            D.comment("このエリアの主と戦闘しますか？");
            D.yes_no("");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            if (yes_no_selected == 1) {
                triger();
            }
        }
        void triger () {
            enemy_num = 2;
            enemys = new Enemy[2];
            int exp_X = 0;
            enemys[0] = new Enemy("古代兵器", 30,3500,150,150,40,170,80,100,10,10,10,10);
            enemys[0].status_set();
            enemys[0].cul_EXP();
            enemys[0].get_first_skill(new Eattack());
            enemys[0].get_skill(new HealSelf());
            enemys[1] = new Enemy("古代兵器", 30,3500,150,150,40,170,80,100,10,10,10,10);
            enemys[1].status_set();
            enemys[1].cul_EXP();
            enemys[1].get_first_skill(new Eattack());
            enemys[1].get_skill(new EAtackall());
            exp_X = 2000;
            Battle battle_X = new Battle(enemy_num);
            int result = battle_X.battle_main();
            if (result == 1) {
                D.comment("モンスターから逃げた。");
            }
            else if (result == 2) {
                D.comment("モンスターに勝利した！");
                P.getEXP(exp_X);
                experienced = true;
                stages[3] = new Stage4();
            }
            else if (result == 3) {
                D.comment("GAME OVER");
                D.finish();
            }
        }
    }

    class Boss_4 extends Event {
        Boss_4() {
            experienced = false;
            event_num = 0;
        }
        void execute() {
            D.comment("このエリアの主と戦闘しますか？");
            D.yes_no("");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            if (yes_no_selected == 1) {
                triger();
            }
        }
        void triger () {
            enemy_num = 1;
            enemys = new Enemy[1];
            int exp_X = 0;
            enemys[0] = new Enemy("魔王", 70,34000,370,370,350,300,200,500,30,40,10,10);
            enemys[0].status_set();
            enemys[0].cul_EXP();
            enemys[0].get_first_skill(new Eattack());
            enemys[0].get_skill(new EThunder());
            enemys[0].get_skill(new EHealAttack());
            enemys[0].get_skill(new HealSelf());
            enemys[0].get_skill(new Healmp());
            enemys[0].get_skill(new Yearn("魔王は余裕を浮かべている"));
            exp_X = enemys[0].EXP;
            Battle battle_X = new Battle(enemy_num);
            int result = battle_X.battle_main();
            if (result == 1) {
                D.comment("モンスターから逃げた。");
            }
            else if (result == 2) {
                D.comment("モンスターに勝利した！");
                P.getEXP(exp_X);
                experienced = true;
                stages[5] = new Stage6();
            }
            else if (result == 3) {
                D.comment("GAME OVER");
                D.finish();
            }
        }
    }

    class Boss_5 extends Event {
        Boss_5() {
            experienced = false;
            event_num = 0;
        }
        void execute() {
            D.comment("このエリアの主と戦闘しますか？");
            D.yes_no("");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            if (yes_no_selected == 1) {
                triger();
            }
        }
        void triger () {
            enemy_num = 1;
            enemys = new Enemy[1];
            int exp_X = 0;
            enemys[0] = new Enemy("魔戦士ラゴス", 80,63300,430,430,430,430,430,600,40,50,50,50);
            enemys[0].status_set();
            enemys[0].cul_EXP();
            enemys[0].get_first_skill(new Eattack());
            enemys[0].get_skill(new EHealAttack());
            enemys[0].get_skill(new EAtackall());
            enemys[0].get_skill(new HealSelf());
            exp_X = enemys[0].EXP;
            Battle battle_X = new Battle(enemy_num);
            int result = battle_X.battle_main();
            if (result == 1) {
                D.comment("モンスターから逃げた。");
            }
            else if (result == 2) {
                D.comment("モンスターに勝利した！");
                P.getEXP(exp_X);
                stages[6] = new Stage7();
                experienced = true;
            }
            else if (result == 3) {
                D.comment("GAME OVER");
                D.finish();
            }
        }
    }

    class Boss_6 extends Event {
        Boss_6() {
            experienced = false;
            event_num = 0;
        }
        void execute() {
            D.comment("このエリアの主と戦闘しますか？");
            D.yes_no("");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            if (yes_no_selected == 1) {
                triger();
            }
        }
        void triger () {
            enemy_num = 1;
            enemys = new Enemy[1];
            int exp_X = 0;
            enemys[0] = new Enemy("ヤマタノオロチ", 90,89000,1000,480,480,480,480,1000,50,50,50,50);
            enemys[0].status_set();
            enemys[0].cul_EXP();
            enemys[0].get_first_skill(new Eattack());
            enemys[0].get_skill(new EAtackall());
            enemys[0].get_skill(new Buff3());
            enemys[0].get_skill(new Healmp());
            enemys[0].get_skill(new HealSelf());
            exp_X = enemys[0].EXP;
            Battle battle_X = new Battle(enemy_num);
            int result = battle_X.battle_main();
            if (result == 1) {
                D.comment("モンスターから逃げた。");
            }
            else if (result == 2) {
                D.comment("モンスターに勝利した！");
                P.getEXP(exp_X);
                experienced = true;
                stages[7] = new Stage8();
            }
            else if (result == 3) {
                D.comment("GAME OVER");
                D.finish();
            }
        }
    }

    class Boss_7 extends Event {
        Boss_7() {
            experienced = false;
            event_num = 0;
        }
        void execute() {
            D.comment("このエリアの主と戦闘しますか？");
            D.yes_no("");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            if (yes_no_selected == 1) {
                triger();
            }
        }
        void triger () {
            enemy_num = 1;
            enemys = new Enemy[1];
            int exp_X = 0;
            enemys[0] = new Enemy("大魔王ハデス", 120,120000,500,700,700,700,650,1200,100,100,100,100);
            enemys[0].status_set();
            enemys[0].cul_EXP();
            enemys[0].get_first_skill(new Eattack());
            enemys[0].get_skill(new EAtackall());
            enemys[0].get_skill(new EThunder());
            enemys[0].get_skill(new EHealAttack());
            enemys[0].get_skill(new HealSelf());
            enemys[0].get_skill(new Buff3());
            enemys[0].get_skill(new Healmp());
            exp_X = enemys[0].EXP;
            Battle battle_X = new Battle(enemy_num);
            int result = battle_X.battle_main();
            if (result == 1) {
                D.comment("モンスターから逃げた。");
            }
            else if (result == 2) {
                D.comment("モンスターに勝利した！");
                P.getEXP(exp_X);
                experienced = true;
            }
            else if (result == 3) {
                D.comment("GAME OVER");
                D.finish();
            }
        }
    }

    abstract class Stage implements Serializable {
        String name;
        int count;
        int field_count;
        Field[] fields;
        Event[] events;
        void add_field(Field new_field) {
            this.fields[field_count] = new_field;
            field_count++;
        }
    }
    
    class Stage1 extends Stage {
        Stage1() {
            this.name = "草原";
            this.count = 0;
            this.field_count = 1;
            this.events = new Event[6];
            this.events[0] = new Startstory();
            this.events[1] = new NewField12();
            this.events[2] = new addType3();
            this.events[3] = new NewField13();
            this.events[4] = new Boss_1();
            this.events[5] = new addType2();
            this.fields = new Field[3];
            this.fields[0] = new Field11(this);

        }
    }

    class Stage2 extends Stage {
        Stage2() {
            this.name = "密林";
            this.count = 0;
            this.field_count = 1;
            this.events = new Event[5];
            this.events[0] = new NewField22();
            this.events[1] = new addType5();
            this.events[2] = new NewField23();
            this.events[3] = new Boss_2();
            this.events[4] = new addType4();
            this.fields = new Field[3];
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stage_num++;
            this.fields[0] = new Field21(this);

        }
    }

    class Stage3 extends Stage {
        Stage3() {
            this.name = "砂漠";
            this.count = 0;
            this.field_count = 1;
            this.events = new Event[4];
            this.events[0] = new NewField32();
            this.events[1] = new addType6();
            this.events[2] = new NewField33();
            this.events[3] = new Boss_3();
            this.fields = new Field[3];
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stage_num++;
            this.fields[0] = new Field31(this);

        }
    }

    class Stage4 extends Stage {
        Stage4() {
            this.name = "魔王城前";
            this.count = 0;
            this.field_count = 1;
            this.events = new Event[3];
            this.events[0] = new NewField42();
            this.events[1] = new addType7();
            this.events[2] = new NewStage5();
            this.fields = new Field[2];
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stage_num++;
            this.fields[0] = new Field41(this);
        }
    }

    class Stage5 extends Stage {
        Stage5() {
            this.name = "魔王城";
            this.count = 0;
            this.field_count = 1;
            this.events = new Event[3];
            this.events[0] = new NewField52();
            this.events[1] = new NewField53();
            this.events[2] = new Boss_4();
            this.fields = new Field[3];
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stage_num++;
            this.fields[0] = new Field51(this);

        }
    }

    class Stage6 extends Stage {
        Stage6() {
            this.name = "魔界";
            this.count = 0;
            this.field_count = 1;
            this.events = new Event[4];
            this.events[0] = new NewField62();
            this.events[1] = new addType8();
            this.events[2] = new NewField63();
            this.events[3] = new Boss_5();
            this.fields = new Field[3];
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stage_num++;
            this.fields[0] = new Field61(this);

        }
    }

    class Stage7 extends Stage {
        Stage7() {
            this.name = "邪悪な山";
            this.count = 0;
            this.field_count = 1;
            this.events = new Event[4];
            this.events[0] = new NewField72();
            this.events[1] = new NewField73();
            this.events[2] = new NewField74();
            this.events[3] = new Boss_6();
            this.fields = new Field[3];
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stage_num++;
            this.fields[0] = new Field71(this);

        }
    }

    class Stage8 extends Stage {
        Stage8() {
            this.name = "魔神の塔";
            this.count = 0;
            this.field_count = 1;
            this.events = new Event[3];
            this.events[0] = new NewField82();
            this.events[1] = new NewField83();
            this.events[2] = new Boss_7();
            this.fields = new Field[3];
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stage_num++;
            this.fields[0] = new Field81(this);

        }
    }

    abstract class Field implements Serializable {
        String name;
        Stage parent_stage;
        int count;
        int max_enemy_count;
        int enemy_spices;
        Enemy[] enemy_spice;
        void name_out() {
            D.comment("〜"+parent_stage.name+"〜"+name+"〜");
        }

        void start_battle() {
            int enemy_count = rand(max_enemy_count) + 1;
            enemy_num = enemy_count;
            enemys = new Enemy[enemy_count];
            int exp_X = 0;
            for (int k=0;k<enemy_count;k++) {
                int num = rand(enemy_spices);
                enemys[k] = new Enemy(enemy_spice[num]);
                enemys[k].status_set();
                enemys[k].cul_EXP();
                exp_X += enemys[k].EXP;
            }
            Battle battle_X = new Battle(enemy_num);
            int result = battle_X.battle_main();
            if (result == 1) {
                D.comment("モンスターから逃げた。");
            }
            else if (result == 2) {
                D.comment("モンスターに勝利した！");
                P.getEXP(exp_X);
                count += enemy_count;
                parent_stage.count += enemy_count;
            }
            else if (result == 3) {
                D.comment("GAME OVER");
                D.finish();
            }
        }

        void menu_select() {
            while(true) {
                D.menu();
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (command_selected < 2) {
                    break;
                } else if (command_selected == 2) {
                    GameSaveLoad.saveProgress(Game.this);
                } else if (command_selected == 3) {
                    P.change_name();
                } else if (command_selected == 4) {
                    P.change();
                }
            }
        }

        abstract void action();

    }

    class Field11 extends Field {
        Field11(Stage s) {
            this.name = "始まりの村";
            this.parent_stage = s;
            this.count = 0;
            this.max_enemy_count = 3;
        }
        void action() {
            if (!parent_stage.events[0].experienced) {
                parent_stage.events[0].execute();
            }
            if (!parent_stage.events[1].experienced) {
                parent_stage.events[1].execute();
            }
            D.comment("いらっしゃいませ！！");
            D.comment("休んでいく？");
            D.yes_no("");
            try {
                waitForButtonClick();
            } catch (InterruptedException e) {
            }
            //D.all_button_invisible();
            if (yes_no_selected == 1) {
                P.rest();
                D.comment("体力が回復した");
                D.set_HP_player();
            }
            D.comment("またいらっしゃい");
        }


    }

    class Field12 extends Field {
        Field12(Stage s) {
            this.name = "村のはずれ";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("スライム", 1,6,10,2,1,2,2,3,2,2,2,2);
            this.enemy_spice[1] = new Enemy("クロネコ", 1,5,10,15,1,1,8,3,2,2,2,2);
            this.enemy_spice[2] = new Enemy("子ゾンビ", 1, 10,10,8,1,2,1,3,2,2,2,2);
            this.max_enemy_count = 2;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 10 && !parent_stage.events[3].experienced) {
                    parent_stage.events[3].execute();
                }
                if (parent_stage.count > 50 && !parent_stage.events[2].experienced) {
                    parent_stage.events[2].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {

                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field13 extends Field {
        Field13(Stage s) {
            this.name = "草原の洞窟";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("スライム", 4,20,15,8,1,8,8,3,2,2,2,2);
            this.enemy_spice[1] = new Enemy("クロネコ", 4,18,15,25,1,8,20,3,2,2,2,2);
            this.enemy_spice[2] = new Enemy("子ゾンビ", 4,35,20,10,1,10,10,3,2,2,2,2);
            this.max_enemy_count = 2;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                if (parent_stage.count > 30 && !parent_stage.events[4].experienced) {
                    parent_stage.events[4].execute();
                }
                if (parent_stage.events[4].experienced && !parent_stage.events[5].experienced) {
                    parent_stage.events[5].execute();
                }

                start_battle();
                if (parent_stage.count > 50 && !parent_stage.events[2].experienced) {
                    parent_stage.events[2].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field21 extends Field {
        Field21(Stage s) {
            this.name = "沼地";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("大スライム", 10,40,20,20,30,30,20,5,5,5,5,5);
            this.enemy_spice[1] = new Enemy("ブルーパンサー", 10,50,15,40,20,30,40,5,5,5,5,5);
            this.enemy_spice[2] = new Enemy("巨大蜂", 10,30,20,50,10,10,50,5,5,5,5,5);
            this.max_enemy_count = 2;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 20 && !parent_stage.events[0].experienced) {
                    parent_stage.events[0].execute();
                }
                if (parent_stage.count > 70 && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field22 extends Field {
        Field22(Stage s) {
            this.name = "丘";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("パンダ", 20,60,30,40,1,40,50,8,8,8,8,8);
            this.enemy_spice[1] = new Enemy("トラ", 20,65,45,45,1,20,40,8,8,8,8,8);
            this.enemy_spice[2] = new Enemy("赤ヘビ", 20,50,20,70,1,30,60,8,8,8,8,8);
            this.max_enemy_count = 3;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 40 && !parent_stage.events[2].experienced) {
                    parent_stage.events[2].execute();
                }
                if (parent_stage.count > 70 && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field23 extends Field {
        Field23(Stage s) {
            this.name = "最深部";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("子ドラゴン", 23,80,50,60,40,40,50,10,10,10,10,10);
            this.enemy_spice[1] = new Enemy("白ゴリラ", 23,130,10,70,1,50,80,10,10,10,10,10);
            this.enemy_spice[2] = new Enemy("黒ゴリラ", 23,130,10,50,1,70,80,10,10,10,10,10);
            this.max_enemy_count = 3;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                if (parent_stage.count > 60 && !parent_stage.events[3].experienced) {
                    parent_stage.events[3].execute();
                }
                if (parent_stage.events[3].experienced && !parent_stage.events[4].experienced) {
                    parent_stage.events[4].execute();
                }

                start_battle();
                if (parent_stage.count > 70 && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }
    }

    class Field31 extends Field {
        Field31(Stage s) {
            this.name = "砂山";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("スナヘビ", 25,120,100,50,1,90,40,12,12,12,12,12);
            this.enemy_spice[1] = new Enemy("スナラクダ", 25,130,15,80,1,40,80,12,12,12,12,12);
            this.enemy_spice[2] = new Enemy("ゾンビ", 25,150,70,70,1,70,50,13,12,12,12,12);
            this.max_enemy_count = 3;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 30 && !parent_stage.events[0].experienced) {
                    parent_stage.events[0].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field32 extends Field {
        Field32(Stage s) {
            this.name = "オアシス";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("スナスライム", 27,150,15,60,60,60,10,14,14,14,15,15);
            this.enemy_spice[1] = new Enemy("カエントカゲ", 27,120,15,100,50,70,100,15,15,15,14,14);
            this.enemy_spice[2] = new Enemy("サボテンマン", 27,200,20,70,1,50,70,14,14,14,14,14);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 45 && !parent_stage.events[2].experienced) {
                    parent_stage.events[2].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field33 extends Field {
        Field33(Stage s) {
            this.name = "古代都市";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("バイソン", 30,220,15,70,70,90,85,16,16,16,16,16);
            this.enemy_spice[1] = new Enemy("スナトカゲ", 30,150,15,110,10,80,120,16,16,16,16,16);
            this.enemy_spice[2] = new Enemy("サマヨイゾンビ", 30,250,20,70,10,60,10,16,16,16,16,16);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                if (parent_stage.count > 60 && !parent_stage.events[3].experienced) {
                    parent_stage.events[3].execute();
                }
                if (parent_stage.events[3].experienced && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                start_battle();
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field41 extends Field {
        Field41(Stage s) {
            this.name = "三日月橋";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("オドリワニ", 33,300,200,160,80,100,100,18,18,18,18,18);
            this.enemy_spice[1] = new Enemy("フェアリーウッド", 33,330,100,80,10,10,20,18,18,18,18,18);
            this.enemy_spice[2] = new Enemy("ホネサカナ", 33,250,200,100,100,140,110,18,18,18,18,18);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 30 && !parent_stage.events[0].experienced) {
                    parent_stage.events[0].execute();
                }
                if (parent_stage.count > 50 && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field42 extends Field {
        Field42(Stage s) {
            this.name = "城門";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("悪魔スライム", 35,280,100,180,180,90,120,20,20,20,20,20);
            this.enemy_spice[1] = new Enemy("死体兵士", 35,340,170,100,100,10,20,20,20,20,20,20);
            this.enemy_spice[2] = new Enemy("オバケドリ", 35,300,150,150,100,80,180,20,20,20,20,20);
            this.max_enemy_count = 2;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 70 && !parent_stage.events[2].experienced) {
                    parent_stage.events[2].execute();
                }
                if (parent_stage.count > 50 && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field51 extends Field {
        Field51(Stage s) {
            this.name = "1F";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("守護兵", 38,400,100,190,100,150,10,22,22,22,22,22);
            this.enemy_spice[1] = new Enemy("魔女", 38,300,230,230,230,120,160,22,22,22,22,22);
            this.enemy_spice[2] = new Enemy("ガイコツ戦士", 38,350,220,220,10,140,140,22,22,22,22,22);
            this.max_enemy_count = 3;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new EAtackall());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[1].get_skill(new EThunder());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 30 && !parent_stage.events[0].experienced) {
                    parent_stage.events[0].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field52 extends Field {
        Field52(Stage s) {
            this.name = "2F";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("ウィザード", 40,380,200,250,300,120,190,22,22,22,22,22);
            this.enemy_spice[1] = new Enemy("ドラゴンゾンビ", 40,450,400,200,100,140,100,23,22,22,22,22);
            this.enemy_spice[2] = new Enemy("ゴーレム", 40,500,10,150,10,190,100,23,22,22,22,22);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new EThunder());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 50 && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    break;
                }
            }
        }

    }

    class Field53 extends Field {
        Field53(Stage s) {
            this.name = "玉座の間";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("バーバリアン", 42,530,200,200,15,200,190,25,25,25,25,25);
            this.enemy_spice[1] = new Enemy("ドラゴンスライム", 42,500,150,220,170,170,160,25,25,25,25,25);
            this.enemy_spice[2] = new Enemy("子ヴァンパイア", 42,480,260,260,260,210,210,25,25,25,25,25);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new EAtackall());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                if (parent_stage.count > 80 && !parent_stage.events[2].experienced) {
                    parent_stage.events[2].execute();
                }
                start_battle();
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }
    }

    class Field61 extends Field {
        Field61(Stage s) {
            this.name = "現世との狭間";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("タマシイ", 45,500,200,210,210,200,200,30,30,30,30,30);
            this.enemy_spice[1] = new Enemy("ノロイニンギョウ", 45,550,250,180,180,150,100,30,30,30,30,30);
            this.enemy_spice[2] = new Enemy("死神蝶", 45,350,330,330,330,120,250,30,30,30,30,30);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 30 && !parent_stage.events[0].experienced) {
                    parent_stage.events[0].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field62 extends Field {
        Field62(Stage s) {
            this.name = "魔族の村";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("悪魔の子", 47,600,300,350,350,120,380,30,30,30,30,30);
            this.enemy_spice[1] = new Enemy("ヴァンパイア", 47,660,300,300,300,350,250,28,28,28,28,28);
            this.enemy_spice[2] = new Enemy("子ヴァンパイア", 42,480,260,260,260,210,210,25,25,25,25,25);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new EThunder());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
            this.enemy_spice[2].get_skill(new EHealAttack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 60 && !parent_stage.events[2].experienced) {
                    parent_stage.events[2].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field63 extends Field {
        Field63(Stage s) {
            this.name = "焼け野原";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("悪魔の子", 47,600,300,350,350,120,380,30,30,30,30,30);
            this.enemy_spice[1] = new Enemy("邪悪なタマシイ", 47,660,300,300,300,350,250,28,28,28,28,28);
            this.enemy_spice[2] = new Enemy("ノロイ", 47,500,260,260,400,210,210,25,25,25,25,25);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new EThunder());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                if (parent_stage.count > 100 && !parent_stage.events[3].experienced) {
                    parent_stage.events[3].execute();
                }
                if (parent_stage.events[3].experienced && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                start_battle();
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field71 extends Field {
        Field71(Stage s) {
            this.name = "麓";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("メタルホース", 50,400,300,450,300,500,450,35,35,35,35,35);
            this.enemy_spice[1] = new Enemy("ブラッドキャット", 50,480,200,360,400,320,400,35,35,35,35,35);
            this.enemy_spice[2] = new Enemy("ドラゴンゾンビ", 50,630,100,300,100,300,300,35,35,35,35,35);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 30 && !parent_stage.events[0].experienced) {
                    parent_stage.events[0].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field72 extends Field {
        Field72(Stage s) {
            this.name = "中腹";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("ブラッドベア", 52,670,100,300,200,40,400,35,35,35,35,35);
            this.enemy_spice[1] = new Enemy("天馬", 52,540,100,430,430,360,600,35,35,35,35,35);
            this.enemy_spice[2] = new Enemy("さまよいスケルトン", 52,300,210,400,10,200,420,35,35,35,35,35);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new EAtackall());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[1].get_skill(new HealSelf());
            this.enemy_spice[2].get_first_skill(new Eattack());
            this.enemy_spice[2].get_skill(new EHealAttack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 60 && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field73 extends Field {
        Field73(Stage s) {
            this.name = "大空洞";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("ブラッドバット", 54,680,200,420,420,350,350,37,37,37,37,37);
            this.enemy_spice[1] = new Enemy("デビルワーム", 54,730,100,380,380,380,200,37,37,37,37,37);
            this.enemy_spice[2] = new Enemy("パールシェル", 54,300,100,400,100,800,10,37,37,37,37,37);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[1].get_skill(new EAtackall());
            this.enemy_spice[2].get_first_skill(new Eattack());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 100 && !parent_stage.events[2].experienced) {
                    parent_stage.events[2].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field74 extends Field {
        Field74(Stage s) {
            this.name = "地獄峠";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("死の骸", 55,600,15,500,500,400,400,40,40,40,40,40);
            this.enemy_spice[1] = new Enemy("地獄の番人", 55,730,15,450,450,450,450,40,40,40,40,40);
            this.enemy_spice[2] = new Enemy("大蛇のしもべ", 55,700,20,400,400,300,500,40,40,40,40,40);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new Buff2());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[1].get_skill(new HealSelf());
            this.enemy_spice[2].get_first_skill(new Eattack());
            this.enemy_spice[2].get_skill(new HealSelf());
        }

        void action() {
            name_out();
            while (true) {
                if (parent_stage.count > 120 && !parent_stage.events[3].experienced) {
                    parent_stage.events[3].execute();
                }
                start_battle();
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field81 extends Field {
        Field81(Stage s) {
            this.name = "1F-5F";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("呪いの騎士", 60,800,100,700,500,700,400,45,45,45,45,45);
            this.enemy_spice[1] = new Enemy("地獄の番人", 60,850,100,650,650,720,540,45,45,45,45,45);
            this.enemy_spice[2] = new Enemy("ゾンビキング", 60,960,100,570,700,600,340,45,45,45,45,45);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new Buff2());
            this.enemy_spice[0].get_skill(new EAtackall());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[1].get_skill(new Buff2());
            this.enemy_spice[1].get_skill(new EHealAttack());
            this.enemy_spice[2].get_first_skill(new Eattack());
            this.enemy_spice[2].get_skill(new Buff2());
            this.enemy_spice[2].get_skill(new HealSelf());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 30 && !parent_stage.events[0].experienced) {
                    parent_stage.events[0].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field82 extends Field {
        Field82(Stage s) {
            this.name = "6F-10F";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("ドラゴンナイト", 65,620,150,890,90,670,740,60,60,60,60,60);
            this.enemy_spice[1] = new Enemy("呪いの騎士", 65,800,100,700,500,700,400,60,60,60,60,60);
            this.enemy_spice[2] = new Enemy("オーガ", 65,860,120,750,750,820,320,60,60,60,60,60);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new Buff3());
            this.enemy_spice[0].get_skill(new EAtackall());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[1].get_skill(new Buff2());
            this.enemy_spice[1].get_skill(new EAtackall());
            this.enemy_spice[2].get_first_skill(new Eattack());
            this.enemy_spice[2].get_skill(new Buff1());
            this.enemy_spice[2].get_skill(new EAtackall());
        }

        void action() {
            name_out();
            while (true) {
                start_battle();
                if (parent_stage.count > 60 && !parent_stage.events[1].experienced) {
                    parent_stage.events[1].execute();
                }
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Field83 extends Field {
        Field83(Stage s) {
            this.name = "最上階";
            this.parent_stage = s;
            this.enemy_spice = new Enemy[3];
            this.enemy_spices = 3;
            this.count = 0;
            this.enemy_spice[0] = new Enemy("キングオーガ", 70,1200,600,840,100,800,500,70,70,70,70,70);
            this.enemy_spice[1] = new Enemy("天空龍", 70,1300,1200,700,700,850,720,70,70,70,70,70);
            this.enemy_spice[2] = new Enemy("ヴァンパイアバロン", 940,800,1000,1000,700,660,900,70,70,70,70,70);
            this.max_enemy_count = 4;
            this.enemy_spice[0].get_first_skill(new Eattack());
            this.enemy_spice[0].get_skill(new Buff1());
            this.enemy_spice[0].get_skill(new EAtackall());
            this.enemy_spice[0].get_skill(new HealSelf());
            this.enemy_spice[1].get_first_skill(new Eattack());
            this.enemy_spice[1].get_skill(new Buff3());
            this.enemy_spice[1].get_skill(new EAtackall());
            this.enemy_spice[1].get_skill(new EThunder());
            this.enemy_spice[2].get_first_skill(new Eattack());
            this.enemy_spice[2].get_skill(new EHealAttack());
            this.enemy_spice[2].get_skill(new HealSelf());
        }

        void action() {
            name_out();
            while (true) {
                if (parent_stage.count > 0 && !parent_stage.events[2].experienced) {
                    parent_stage.events[2].execute();
                }
                start_battle();
                D.yes_no("続けてたたかう？");
                try {
                    waitForButtonClick();
                } catch (InterruptedException e) {
                    
                }
                if (yes_no_selected != 1) {
                    menu_select();
                    if (command_selected == 1) {
                        break;
                    }
                }
            }
        }

    }

    class Startstory extends Event {
        Startstory() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("この世界は魔王によって支配されてしまいました。");
            D.comment("あなたに宿る勇者の力で世界を救ってください！");
            D.comment("この村では体力を回復できます。");
            D.comment("メニューから移動して冒険に出ましょう!!");
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField12 extends Event {
        NewField12() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[0].add_field(new Field12(stages[0]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField13 extends Event {
        NewField13() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[0].add_field(new Field13(stages[0]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField22 extends Event {
        NewField22() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[1].add_field(new Field22(stages[1]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField23 extends Event {
        NewField23() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[1].add_field(new Field23(stages[1]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField32 extends Event {
        NewField32() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[2].add_field(new Field32(stages[2]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField33 extends Event {
        NewField33() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[2].add_field(new Field33(stages[2]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField42 extends Event {
        NewField42() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[3].add_field(new Field42(stages[3]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField52 extends Event {
        NewField52() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[4].add_field(new Field52(stages[4]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField53 extends Event {
        NewField53() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[4].add_field(new Field53(stages[4]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField62 extends Event {
        NewField62() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[5].add_field(new Field62(stages[5]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField63 extends Event {
        NewField63() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[5].add_field(new Field63(stages[5]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField72 extends Event {
        NewField72() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[6].add_field(new Field72(stages[6]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField73 extends Event {
        NewField73() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[6].add_field(new Field73(stages[6]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField74 extends Event {
        NewField74() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[6].add_field(new Field74(stages[6]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField82 extends Event {
        NewField82() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[7].add_field(new Field82(stages[7]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewField83 extends Event {
        NewField83() {
            experienced = false;
            event_num = 1;
        }
        void execute() {
            D.comment("新しいエリアが開放されました！");
            D.comment("メニューから移動できます");
            stages[7].add_field(new Field83(stages[7]));
            experienced = true;
        }
        void triger(){

        }
    }

    class NewStage5 extends Event {
        NewStage5() {
            experienced = false;
            event_num = 1;
        }

        void execute() {
            stages[4] = new Stage5();
        }
        void triger() {}
    }

}

public class Main {
    public static void main(String[] args) throws IOException {
        Game G = new Game();
        G.start_story();
        if (G.command_selected == 1) {
            G.first();
        } else {
            Data progress = GameSaveLoad.loadProgress();
            if (progress != null) {
                G = progress.get_game();
                Game.Display.disp.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                Game.Display.disp.dispose();
                G.D = G.new Display();
                G.D.validall();
                G.D.gui1();
                G.D.status_main();
                //System.out.println(G.stages[4].fields[0].enemy_spice[0].max_MP);
            } else {
                G.first();
            }
            
        }
        G.stage_select();
    }
}