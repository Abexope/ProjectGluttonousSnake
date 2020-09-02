import javax.swing.*;

public class MSnake {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setBounds(510, 200, 900, 700);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // 点击关闭按钮时的动作：关闭应用
        frame.add(new MPanel());    // 在界面上添加画布
        frame.setVisible(true);
    }
}
