
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class MPanel extends JPanel implements KeyListener, ActionListener {

    ImageIcon title;
    ImageIcon body;
    ImageIcon up;
    ImageIcon down;
    ImageIcon left;
    ImageIcon right;
    ImageIcon food;
    ImageIcon high_food;
    ImageIcon evil_food;

    int foodX;      // 食物坐标X
    int foodY;      // 食物坐标Y
    int highX;
    int highY;
    int evilX;
    int evilY;
    Random rand = new Random();    // 随机数生成器

    boolean changeDiff;     // 难度可变指示

    final int INITIAL_LEN = 3;
    int len;    // 初始长度
    int score;  // 游戏分数
    int[] snakeX;    // 蛇身坐标数组X
    int[] snakeY;    // 蛇身坐标数组Y
    String direction = "R";         // 蛇头初始方向   [R:右/L:左/U:上/D:下]

    boolean isStarted = false;      // 游戏状态，初始为未开始状态
    boolean isFailed = false;       // 游戏失败，初始为未失败状态

    final int INITIAL_DELAY = 200;  // 初始定时时延
    Timer timer = new Timer(INITIAL_DELAY, this);     // 定时器

    Clip bgm;   // 背景音乐

    public MPanel() {
        loadImages();
        initSnake();
        this.setFocusable(true);        // 可以响应键盘事件
        this.addKeyListener(this);   // 添加键盘监听器
        timer.start();                  // 启动定时器

        loadBGM();

    }

    // 加载图片
    private void loadImages() {
        InputStream is;

        try {

            is = this.getClass().getClassLoader().getResourceAsStream("images/title.jpg");
            assert is != null;
            title = new ImageIcon(ImageIO.read(is));

            is = this.getClass().getClassLoader().getResourceAsStream("images/body.png");
            assert is != null;
            body = new ImageIcon(ImageIO.read(is));

            is = this.getClass().getClassLoader().getResourceAsStream("images/up.png");
            assert is != null;
            up = new ImageIcon(ImageIO.read(is));

            is = this.getClass().getClassLoader().getResourceAsStream("images/down.png");
            assert is != null;
            down = new ImageIcon(ImageIO.read(is));

            is = this.getClass().getClassLoader().getResourceAsStream("images/left.png");
            assert is != null;
            left = new ImageIcon(ImageIO.read(is));

            is = this.getClass().getClassLoader().getResourceAsStream("images/right.png");
            assert is != null;
            right = new ImageIcon(ImageIO.read(is));

            is = this.getClass().getClassLoader().getResourceAsStream("images/food.png");
            assert is != null;
            food = new ImageIcon(ImageIO.read(is));

            is = this.getClass().getClassLoader().getResourceAsStream("images/high_food.jpg");
            assert is != null;
            high_food = new ImageIcon(ImageIO.read(is));

            is = this.getClass().getClassLoader().getResourceAsStream("images/evil_food.jpg");
            assert is != null;
            evil_food = new ImageIcon(ImageIO.read(is));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 加载背景音乐
    private void loadBGM() {
        /*
        Exception in thread "main" java.lang.NullPointerException
            at com.sun.media.sound.SoftMidiAudioFileReader.getAudioInputStream(SoftMidiAudioFileReader.java:134)
            at javax.sound.sampled.AudioSystem.getAudioInputStream(AudioSystem.java:1113)
            at MPanel.playBGM(MPanel.java:63)
            at MPanel.<init>(MPanel.java:54)
            at MSnake.main(MSnake.java:10)
        已解决：
            在IDEA中，想导入流媒体数据，必须要先在工程文件的根目录下创建一个源文件夹，并将其 mark as Resource root
            然后才能通过 this.getClass().getClassLoader().getResourceAsStream(路径) 正确导入
        */
        try {
            bgm = AudioSystem.getClip();   // 创建播放器组件
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("media/bgm.wav");
            assert is != null;
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            /* 解决jar包程序中背景音乐无法播放的问题
                采用缓冲流读取文件对象
            */
            bgm.open(ais);
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    // 播放背景音乐
    private void playBGM(){
        // bgm.start();     // 单曲播放
        bgm.loop(Clip.LOOP_CONTINUOUSLY);   // 单曲循环
    }

    // 暂停背景音乐
    private void stopBGM() {
        bgm.stop();
    }

    // 画组件
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        this.setBackground(Color.WHITE);

        title.paintIcon(this, graphics, 25, 11);
        graphics.fillRect(25, 75, 850, 575);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("arial", Font.BOLD, 15));
        graphics.drawString("len " + len, 750, 35);
        graphics.drawString("score " + score, 750, 50);

        // 画蛇头
        switch (direction) {
            case "R": right.paintIcon(this, graphics, snakeX[0], snakeY[0]); break;
            case "L": left.paintIcon(this, graphics, snakeX[0], snakeY[0]); break;
            case "U": up.paintIcon(this, graphics, snakeX[0], snakeY[0]); break;
            case "D": down.paintIcon(this, graphics, snakeX[0], snakeY[0]); break;
        }
        // 画蛇身
        for (int i = 1; i < len; i++) {
            body.paintIcon(this, graphics, snakeX[i], snakeY[i]);
        }
        // 画食物
        if (score < 5) {
            food.paintIcon(this, graphics, foodX, foodY);
        } else {
            food.paintIcon(this, graphics, foodX, foodY);
            high_food.paintIcon(this, graphics, highX, highY);
            evil_food.paintIcon(this, graphics, evilX, evilY);
        }


        // 游戏开始提示
        if (!isStarted) {
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("arial", Font.BOLD, 40));
            graphics.drawString("Press space to start game", 250, 350);
        }

        // 失败重启提示
        if (isFailed) {
            graphics.setColor(Color.RED);
            graphics.setFont(new Font("arial", Font.BOLD, 40));
            graphics.drawString("Failed: Press space to restart game", 125, 350);
        }

    }

    // 蛇体初始化
    private void initSnake() {
        // 分数
        score = 0;
        // 蛇体
        snakeX = new int[750];    // 蛇身坐标数组X
        snakeY = new int[750];    // 蛇身坐标数组Y
        direction = "R";    // 蛇头初始方向   [R:右/L:左/U:上/D:下]
        len = INITIAL_LEN;
        snakeX[0] = 100;
        snakeY[0] = 100;
        snakeX[1] = 75;
        snakeY[1] = 100;
        snakeX[2] = 50;
        snakeY[2] = 100;
        // 食物初始化
        generateNewFood();
        // 定时器
        timer.setDelay(INITIAL_DELAY);
        // 难度可变指示
        changeDiff = false;
    }

    private void generateCommonFood() {
        // 生成普通食物
        foodX = 25 + 25 * rand.nextInt(34);
        foodY = 75 + 25 * rand.nextInt(23);
        // 优化：食物不会生成在蛇身上
        while (true) {
            boolean repeat = false;     // 重复标记
            for (int i = 0; i < len; i++) {
                if ((foodX == snakeX[i]) && (foodY == snakeY[i])) {
                    repeat = true;
                    break;
                }
            }
            if (!repeat)
                break;
            else {
                // 重新生成
                foodX = 25 + 25 * rand.nextInt(34);
                foodY = 75 + 25 * rand.nextInt(23);
            }
        }
    }

    private void generateHighFood() {
        // 生成高能食物
        highX = 25 + 25 * rand.nextInt(34);
        highY = 75 + 25 * rand.nextInt(23);
        // 优化：食物不会生成在蛇身上
        while (true) {
            boolean repeat = false;     // 重复标记
            if ((highX == foodX) && (highY == foodY)) {   // 与普通食物重复
                highX = 25 + 25 * rand.nextInt(34);
                highY = 75 + 25 * rand.nextInt(23);
                continue;
            }
            for (int i = 0; i < len; i++) {
                if ((highX == snakeX[i]) && (highY == snakeY[i])) {
                    repeat = true;
                    break;
                }
            }
            if (!repeat)
                break;
            else {
                // 重新生成
                highX = 25 + 25 * rand.nextInt(34);
                highY = 75 + 25 * rand.nextInt(23);
            }
        }
    }

    private void generateEvilFood() {
        // 生成有毒食物
        evilX = 25 + 25 * rand.nextInt(34);
        evilY = 75 + 25 * rand.nextInt(23);
        // 优化：食物不会生成在蛇身上
        while (true) {
            boolean repeat = false;     // 重复标记
            if ((evilX == foodX) && (evilY == foodY)) {   // 与普通食物重复
                evilX = 25 + 25 * rand.nextInt(34);
                evilY = 75 + 25 * rand.nextInt(23);
                continue;
            }
            if ((evilX == highX) && (evilY == highY)) {   // 与高能食物重复
                evilX = 25 + 25 * rand.nextInt(34);
                evilY = 75 + 25 * rand.nextInt(23);
                continue;
            }
            for (int i = 0; i < len; i++) {
                if ((evilX == snakeX[i]) && (evilY == snakeY[i])) {
                    repeat = true;
                    break;
                }
            }
            if (!repeat)
                break;
            else {
                // 重新生成
                evilX = 25 + 25 * rand.nextInt(34);
                evilY = 75 + 25 * rand.nextInt(23);
            }
        }
    }

    // 生成食物
    private void generateNewFood() {

        if (score < 5) {    // 只生成普通食物
            generateCommonFood();
        } else {    // 随机生成高能食物或有毒食物
            // 为验证效果，先全部显示
            generateCommonFood();

            // 高能食物
            generateHighFood();

            // 有毒食物
            generateEvilFood();

        }

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();   // 获取按下的键盘编码

        switch (keyCode) {
            case KeyEvent.VK_SPACE: {   // 空格键的编码：0x20(32)，如果按下空格
                if (isFailed) {
                    isFailed = false;
                    initSnake();
                } else {
                    isStarted = !isStarted;
                }
                this.repaint();
                break;
            }
            case KeyEvent.VK_LEFT: {
                if (direction.equals("U") || direction.equals("D"))     // 头朝上/下时，只能左右转向
                    direction = "L";
                break;
            }
            case KeyEvent.VK_RIGHT: {
                if (direction.equals("U") || direction.equals("D"))     // 头朝上/下时，只能左右转向
                    direction = "R";
                break;
            }
            case KeyEvent.VK_UP: {
                if (direction.equals("L") || direction.equals("R"))     // 头朝左/右时，只能上下转向
                    direction = "U";
                break;
            }
            case KeyEvent.VK_DOWN: {
                if (direction.equals("L") || direction.equals("R"))     // 头朝左/右时，只能上下转向
                    direction = "D";
                break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (this.isStarted && !isFailed) {      // 游戏开始且未失败

            // 播放BGM
            playBGM();

            // 难度递增
            if (changeDiff) {
                changeDiff = false;     // 进入分支后先将开关关闭
                timer.setDelay(Math.max(timer.getDelay() - 50, 100));
            }

            // 蛇身移动
            for (int i = len-1; i > 0; i--) {
                snakeX[i] = snakeX[i-1];
                snakeY[i] = snakeY[i-1];
            }

            // 蛇头移动
            switch (this.direction) {
                case "R": {
                    snakeX[0] += 25;
                    if (snakeX[0] > 850)
                        snakeX[0] = 850;
                    break;
                }
                case "L": {
                    snakeX[0] -= 25;
                    if (snakeX[0] < 25)
                        snakeX[0] = 25;
                    break;
                }
                case "U": {
                    snakeY[0] -= 25;
                    if (snakeY[0] < 75)
                        snakeY[0] = 75;
                    break;
                }
                case "D": {
                    snakeY[0] += 25;
                    if (snakeY[0] > 625)
                        snakeY[0] = 625;
                    break;
                }

            }

            // 吃到食物
            if (snakeX[0] == foodX && snakeY[0] == foodY) {
                len++;
                /* 优化
                    每次len加一的时候可以把新队尾的xy坐标设为前一个xy坐标。
                    否则会是0，导致那一定时间隔在0,0坐标(即左上角)有个body会闪一下。
                */
                supplySnakeTail();
                score += 1;
                if (((len-INITIAL_LEN) > 5) && ((len-INITIAL_LEN) % 5 == 0))     // 每获得5分增加一次难度
                    changeDiff = true;
                generateNewFood();
            }

            // 吃到高能食物
            if (snakeX[0] == highX && snakeY[0] == highY) {
                len += 1;
                supplySnakeTail();
                score += 2;
                generateNewFood();
            }

            // 吃到有毒食物
            if (snakeX[0] == evilX && snakeY[0] == evilY) {
                isFailed = true;
            }

            // 撞墙
            if (snakeX[0] > 850 || snakeX[0] < 25 || snakeY[0] < 75 || snakeY[0] > 625)
                isFailed = true;

            // 撞到自己
            for (int i = 1; i < len; i++) {
                if (snakeX[i] == snakeX[0] && snakeY[i] == snakeY[0]) {
                    isFailed = true;
                    break;
                }
            }

            this.repaint();
        } else {
            // 停止音乐
            stopBGM();
        }


        timer.start();
    }

    private void supplySnakeTail() {
        /* 优化
            每次len加一的时候可以把新队尾的xy坐标设为前一个xy坐标。
            否则会是0，导致那一定时间隔在0,0坐标(即左上角)有个body会闪一下。
        */
        switch(this.direction) {
            case "U":
            case "D":
                snakeY[len - 1 ]-= 25;
                break;
            case "L":
            case "R":
                snakeX[len - 1] -= 25;
                break;
        }
    }

}
