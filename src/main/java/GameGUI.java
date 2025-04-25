package src.main.java;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GameGUI extends JPanel {
    private final int TILE_SIZE = 80;
    private final int BOARD_SIZE = 8;
    private final int BOARD_PIXEL_SIZE = TILE_SIZE * BOARD_SIZE;
    private Piece selectedPiece = null;
    private Color highlightColor = null;
    private GameLogic gameLogic;
    private boolean showAtariScreen = true;
    private boolean showNameInput = false;
    private BufferedImage atariLogo;
    private int selectedRow = -1, selectedCol = -1;
    private boolean gameOver = false;
    private ChessColor winner = null;
    private List<Piece> whiteCaptured;
    private List<Piece> blackCaptured;
    private String playerWhiteName = "White Player";
    private String playerBlackName = "Black Player";
    private boolean isDraw = false;
    private String drawReason = "";


    public GameGUI() {
        loadAtari();
        gameLogic = new GameLogic();
        whiteCaptured = new ArrayList<>();
        blackCaptured = new ArrayList<>();
        try {

            InputStream fontStream = getClass().getClassLoader().getResourceAsStream("assets/PressStart2P-Regular.ttf");
            if (fontStream == null) {
                fontStream = getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf");
            }

            if (fontStream != null) {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(24f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
                fontStream.close();
            } else {
                System.err.println("Font file not found!");
            }
        } catch (Exception e) {
            System.err.println("Error loading font: " + e.getMessage());
            e.printStackTrace();
        }

        Timer atariTimer = new Timer(2000, e -> {
            showAtariScreen = false;
            showNameInput = true;
            getPlayerNames();
            repaint();
        });
        atariTimer.setRepeats(false);
        atariTimer.start();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (gameOver) {
                    resetGame();
                    return;
                }

                if (showAtariScreen || showNameInput) return;

                int col = (e.getX() - (getWidth() - BOARD_PIXEL_SIZE) / 2) / TILE_SIZE;
                int row = (e.getY() - (getHeight() - BOARD_PIXEL_SIZE) / 2) / TILE_SIZE;

                handleMove(row, col);
            }
        });
    }

    private void resetGame() {
        gameLogic = new GameLogic();
        whiteCaptured = new ArrayList<>();
        blackCaptured = new ArrayList<>();
        gameOver = false;
        winner = null;
        isDraw = false;
        drawReason = "";
        selectedRow = -1;
        selectedCol = -1;
        selectedPiece = null;
        highlightColor = null;
        repaint();
    }

    private void handleMove(int row, int col) {
        if (selectedRow == -1 && selectedCol == -1) {
            selectedPiece = gameLogic.getBoard().getPieceAt(new Position(row, col));
            if (row >= 0 && row < BOARD_SIZE &&
                    col >= 0 && col < BOARD_SIZE &&
                    selectedPiece != null &&
                    gameLogic.getTurn() == selectedPiece.getColor()) {
                selectedRow = row;
                selectedCol = col;
                highlightColor = (selectedPiece.getColor() == ChessColor.WHITE) ?
                        new Color(173, 216, 230, 150) : new Color(238, 130, 238, 150);
                repaint();
            }
        } else {
            boolean moved = gameLogic.move(new Position(selectedRow, selectedCol), new Position(row, col));
            selectedRow = -1;
            selectedCol = -1;
            selectedPiece = null;
            highlightColor = null;

            if (moved) {
                updateCapturedPieces();
                checkGameEnd();
            }
            repaint();
        }
    }

    private void updateCapturedPieces() {
        blackCaptured = gameLogic.getCapturedPieces(ChessColor.WHITE);
        whiteCaptured = gameLogic.getCapturedPieces(ChessColor.BLACK);
    }

    private void checkGameEnd() {
        if (gameLogic.isCheckmate()) {
            gameOver = true;
            winner = (gameLogic.getTurn() == ChessColor.WHITE) ? ChessColor.BLACK : ChessColor.WHITE;
        } else if (gameLogic.isDraw()) {
            gameOver = true;
            isDraw = true;
            drawReason = gameLogic.getDrawReason();
        }
    }

    private void loadAtari() {
        try {
            atariLogo = ImageIO.read(new File("assets/atari.jpg"));
        } catch (Exception e) {
            System.out.println("Failed to load Atari startup! ERROR:1");
        }
    }

    private void getPlayerNames() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);

        Font customFont;
        try {
            // Stessa logica di caricamento del font
            InputStream fontStream = getClass().getClassLoader().getResourceAsStream("assets/PressStart2P-Regular.ttf");
            if (fontStream == null) {
                fontStream = getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf");
            }

            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(14f);
                fontStream.close();
            } else {
                throw new Exception("Font not found");
            }
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 14);
            System.err.println("Using fallback font due to: " + e.getMessage());
        }

        JLabel whiteLabel = new JLabel("WHITE PLAYER NAME:");
        whiteLabel.setFont(customFont);
        whiteLabel.setForeground(Color.WHITE);
        panel.add(whiteLabel);

        JTextField whiteField = new JTextField("Player 1", 15);
        styleTextField(whiteField, customFont);
        panel.add(whiteField);

        JLabel blackLabel = new JLabel("BLACK PLAYER NAME:");
        blackLabel.setFont(customFont);
        blackLabel.setForeground(Color.WHITE);
        panel.add(blackLabel);

        JTextField blackField = new JTextField("Player 2", 15);
        styleTextField(blackField, customFont);
        panel.add(blackField);

        JButton okButton = new JButton("START GAME");
        styleButton(okButton, customFont);
        okButton.addActionListener(e -> {
            playerWhiteName = whiteField.getText().trim().isEmpty() ? "White Player" : whiteField.getText();
            playerBlackName = blackField.getText().trim().isEmpty() ? "Black Player" : blackField.getText();
            showNameInput = false;
            Window window = SwingUtilities.getWindowAncestor(panel);
            window.dispose();
            repaint();
        });

        panel.add(new JLabel());
        panel.add(okButton);

        JDialog dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void styleTextField(JTextField field, Font font) {
        field.setFont(font);
        field.setBackground(new Color(255, 255, 255, 200));
        field.setBorder(BorderFactory.createLineBorder(new Color(24, 53, 99), 3));
        field.setForeground(Color.BLACK);
    }

    private void styleButton(JButton button, Font font) {
        button.setFont(font);
        button.setBackground(new Color(24, 53, 99));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.setFocusPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (showAtariScreen) {
            drawAtariScreen(g);
        } else if (showNameInput) {
            drawBackground(g);
        } else if (gameOver) {
            drawBackground(g);
            if (isDraw) {
                drawDrawScreen(g);
            } else {
                drawWinScreen(g);
            }
        } else {
            drawGame(g);
        }
    }
    private void drawGame(Graphics g) {
        drawBackground(g);
        drawPixelBorder(g);
        drawChessBoard(g);
        drawPieces(g);
        drawCapturedPieces(g);

        if (selectedRow != -1 && selectedCol != -1) {
            Piece piece = gameLogic.getBoard().getPieceAt(new Position(selectedRow, selectedCol));

            if (piece != null) {
                if (piece.getColor() == ChessColor.WHITE) {
                    g.setColor(new Color(0xFF8200));
                } else {
                    g.setColor(new Color(0xFF8100));
                }
            } else {
                g.setColor(new Color(0xaa7fe3));
            }

            int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2 + selectedCol * TILE_SIZE;
            int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2 + selectedRow * TILE_SIZE;
            g.drawRect(startX, startY, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawAtariScreen(Graphics g) {
        if (atariLogo != null) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            double scaleX = (double) panelWidth / atariLogo.getWidth();
            double scaleY = (double) panelHeight / atariLogo.getHeight();
            double scale = Math.min(scaleX, scaleY);

            int newWidth = (int) (atariLogo.getWidth() * scale);
            int newHeight = (int) (atariLogo.getHeight() * scale);

            int x = (panelWidth - newWidth) / 2;
            int y = (panelHeight - newHeight) / 2;
            g.drawImage(atariLogo, x, y, newWidth, newHeight, this);
        } else {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Errore nel caricamento del logo Atari.", 50, 50);
        }
    }


    private void drawBackground(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        int boardStartX = (width - BOARD_PIXEL_SIZE) / 2;

        try {
            Image backImage = new ImageIcon(getClass().getResource("/assets/back.png")).getImage();
            g2d.drawImage(backImage, 0, 0, width, height, this);
            GradientPaint gradient = new GradientPaint(0, 0, new Color(240, 240, 240, 100), width / 2, 0, new Color(255, 255, 255, 0));
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width / 2, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        drawImage(g2d, "cometa.png", 60, 210, 80, 80, true);
        drawImage(g2d, "cometa.png", 200, 600, 50, 50, true);
        drawImage(g2d, "cometa.png", 400, 300, 50, 50, false);
        drawImage(g2d, "cometa3.png", 100, 300, 50, 50, false);
        drawImage(g2d, "cometa3.png", 5, 700, 50, 50, true);
        drawImage(g2d, "moon.png", 20, 30, 250, 200);
        drawImage(g2d, "planet.png", boardStartX / 2 - 20, height / 2 - 70, 180, 140);
        drawImage(g2d, "planet.png", 0, 780, 180, 140, true);
        drawImage(g2d, "planet.png", 4, 550, 60, 40, true);
        drawImage(g2d, "planet.png", 250, 200, 70, 60, false);
        drawImage(g2d, "star1.png", boardStartX - 100, 40, 100, 55);
        drawImage(g2d, "star1.png", boardStartX, 90, 45, 45);
        drawImage(g2d, "star1.png", 1, 350, 130, 80);
        drawImage(g2d, "star2.png", boardStartX / 2 - 80, height / 2 + 90, 100, 60);
        drawImage(g2d, "star2.png", 20, height / 2, 100, 60);
        drawImage(g2d, "star2.png", 20, 15, 90, 60);
        drawImage(g2d, "star2.png", 350, 200, 90, 50);
        drawImage(g2d, "star2.png", 350, 750, 80, 50);
        drawImage(g2d, "starunique.png", boardStartX / 2 - 120, height - 190, 150, 90);
        drawImage(g2d, "starunique.png", 200, 260, 150, 100);
        drawImage(g2d, "starunique.png", 300, 600, 150, 100);
        drawImage(g2d, "starunique.png", 300, 500, 80, 60);
        drawImage(g2d, "starunique2.png", boardStartX / 2 + 60, height - 180, 120, 80);
        drawImage(g2d, "starunique2.png", 250, 150, 75, 40);
        drawImage(g2d, "starunique2.png", 50, 700, 75, 40);
        drawImage(g2d, "starunique2.png", 190, 50, 60, 30);
        drawImage(g2d, "starunique2.png", 100, 100, 70, 45);
        drawImage(g2d, "starunique2.png", 230, 250, 45, 25);
        drawImage(g2d, "starunique2.png", 150, 500, 40, 20);
        drawImage(g2d, "starunique2.png", 20, 600, 40, 20);
        drawImage(g2d, "starunique2.png", 320, 100, 50, 25);
        drawImage(g2d, "starunique2.png", 90, 700, 45, 20);
        drawImage(g2d, "starunique2.png", 250, 50, 40, 20);
        drawImage(g2d, "starunique2.png", 300, 400, 50, 25);
        drawImage(g2d, "starunique2.png", 180, 350, 45, 20);
        drawImage(g2d, "starunique2.png", 400, 500, 40, 20);
        drawImage(g2d, "starunique2.png", 150, 600, 35, 20);
        drawImage(g2d, "starunique2.png", 90, 450, 50, 25);
        drawImage(g2d, "starunique2.png", 320, 200, 45, 25);
        drawImage(g2d, "starwow.png", 100, 200, 30, 15);
        drawImage(g2d, "starwow.png", 250, 100, 30, 15);
        drawImage(g2d, "starwow.png", 150, 400, 25, 12);
        drawImage(g2d, "starwow.png", 20, 500, 30, 15);
        drawImage(g2d, "starwow.png", 320, 150, 35, 18);
        drawImage(g2d, "starwow.png", 70, 600, 30, 15);
        drawImage(g2d, "starwow.png", 200, 50, 30, 15);
        drawImage(g2d, "starwow.png", 300, 300, 35, 18);
        drawImage(g2d, "starwow.png", 250, 600, 90, 50);
        drawImage(g2d, "starwow.png", 250, 800, 110, 70);

    }

    private void drawImage(Graphics2D g2d, String file, int x, int y, int w, int h) {
        drawImage(g2d, file, x, y, w, h, false);
    }

    private void drawImage(Graphics2D g2d, String file, int x, int y, int w, int h, boolean flip) {
        try {
            BufferedImage img = ImageIO.read(new File("assets/" + file));
            if (img == null) return;

            img = img.getSubimage(5, 5, img.getWidth() - 10, img.getHeight() - 10);

            if (flip) {
                g2d.drawImage(img, x + w, y, -w, h, null);
            } else {
                g2d.drawImage(img, x, y, w, h, null);
            }
        } catch (Exception e) {
            System.err.println("Errore caricando " + file);
        }
    }


    private void drawPixelBorder(Graphics g) {
        g.setColor(new Color(30, 18, 48));
        int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2;
        int endX = startX + BOARD_PIXEL_SIZE;
        int endY = startY + BOARD_PIXEL_SIZE;
        int pixelSize = 10;

        for (int x = startX - pixelSize; x < endX + pixelSize; x += pixelSize) {
            g.fillRect(x, startY - pixelSize, pixelSize, pixelSize);
            g.fillRect(x, endY, pixelSize, pixelSize);
        }

        for (int y = startY; y < endY; y += pixelSize) {
            g.fillRect(startX - pixelSize, y, pixelSize, pixelSize);
            g.fillRect(endX, y, pixelSize, pixelSize);
        }
    }

    private void drawChessBoard(Graphics g) {
        Color dark = new Color(0x18254a);
        Color light = new Color(245, 238, 220);

        int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isDark = (row + col) % 2 != 0;
                g.setColor(isDark ? dark : light);
                g.fillRect(startX + col * TILE_SIZE, startY + row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawPieces(Graphics g) {
        int startX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int startY = (getHeight() - BOARD_PIXEL_SIZE) / 2;
        double scaleFactor = 0.85;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = gameLogic.getBoard().getPieceAt(new Position(row, col));
                if (piece != null) {
                    BufferedImage pieceImg = piece.getTexture();
                    int pieceWidth = (int) (TILE_SIZE * scaleFactor);
                    int pieceHeight = (int) (TILE_SIZE * scaleFactor);
                    BufferedImage resizedPiece = resizeImage(pieceImg, pieceWidth, pieceHeight);
                    int x = startX + col * TILE_SIZE + (TILE_SIZE - pieceWidth) / 2;
                    int y = startY + row * TILE_SIZE + (TILE_SIZE - pieceHeight) / 2;
                    g.drawImage(resizedPiece, x, y, null);
                }
            }
        }
    }


    private void drawCapturedPieces(Graphics g) {
        int iconSize = 40;
        int padding = 8;
        int windowWidth = 250;
        int marginFromBoard = 35;

        //PER LA SCACCHIERA
        int boardStartX = (getWidth() - BOARD_PIXEL_SIZE) / 2;
        int boardStartY = (getHeight() - BOARD_PIXEL_SIZE) / 2;

        //finestrella --
        int windowX = boardStartX + BOARD_PIXEL_SIZE + marginFromBoard;
        int windowY = boardStartY;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRoundRect(windowX, windowY, windowWidth, BOARD_PIXEL_SIZE, 20, 20);

        g2d.setColor(new Color(24, 53, 99));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(windowX, windowY, windowWidth, BOARD_PIXEL_SIZE, 20, 20);

        g2d.setFont(new Font("Press Start 2P", Font.PLAIN, 12));
        int textLeft = windowX + 15;

        g2d.setColor(Color.BLACK);
        g2d.drawString(playerWhiteName + " captured:", textLeft, windowY + 25);

        for (int i = 0; i < whiteCaptured.size(); i++) {
            BufferedImage img = resizeImage(whiteCaptured.get(i).getTexture(), iconSize, iconSize);
            int x = textLeft + (i % 5) * (iconSize + padding);
            int y = windowY + 35 + (i / 5) * (iconSize + padding);
            g2d.drawImage(img, x, y, null);
        }

        //LINEA DIVISORIA ------
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawLine(windowX + 10, windowY + BOARD_PIXEL_SIZE/2,
                windowX + windowWidth - 10, windowY + BOARD_PIXEL_SIZE/2);
        g2d.setColor(Color.BLACK);
        g2d.drawString(playerBlackName + " captured:", textLeft, windowY + BOARD_PIXEL_SIZE/2 + 25);

        for (int i = 0; i < blackCaptured.size(); i++) {
            BufferedImage img = resizeImage(blackCaptured.get(i).getTexture(), iconSize, iconSize);
            int x = textLeft + (i % 5) * (iconSize + padding);
            int y = windowY + BOARD_PIXEL_SIZE/2 + 35 + (i / 5) * (iconSize + padding);
            g2d.drawImage(img, x, y, null);
        }
    }

    private void drawDrawScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        try {
            Font customFont;
            InputStream fontStream = getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf");

            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(48f);
                g2d.setFont(customFont);
                fontStream.close();
            } else {
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                System.err.println("Font non trovato, usando fallback");
            }
        } catch (Exception e) {
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            System.err.println("Errore caricamento font: " + e.getMessage());
        }

        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();

        String drawText = "DRAW!";
        int textWidth = fm.stringWidth(drawText);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2 - 50;
        g2d.drawString(drawText, x, y);


        g2d.setFont(g2d.getFont().deriveFont(24f));
        fm = g2d.getFontMetrics();
        String reasonText = "(" + drawReason + ")";
        textWidth = fm.stringWidth(reasonText);
        x = (getWidth() - textWidth) / 2;
        y = getHeight() / 2 + 10;
        g2d.drawString(reasonText, x, y);

        String restartText = "Click anywhere to restart";
        textWidth = fm.stringWidth(restartText);
        x = (getWidth() - textWidth) / 2;
        y = getHeight() / 2 + 50;
        g2d.drawString(restartText, x, y);
    }

    private void drawWinScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        String winText = (winner == ChessColor.WHITE) ?
                playerWhiteName + " WINS!" : playerBlackName + " WINS!";
        g2d.setColor(Color.WHITE);

        try {
            Font customFont;
            InputStream fontStream = getClass().getResourceAsStream("/assets/PressStart2P-Regular.ttf");

            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(48f);
                g2d.setFont(customFont);
                fontStream.close();
            } else {
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                System.err.println("Font non trovato, usando fallback");
            }
        } catch (Exception e) {
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            System.err.println("Errore caricamento font: " + e.getMessage());
        }

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(winText);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2;
        g2d.drawString(winText, x, y);


        g2d.setFont(new Font(g2d.getFont().getName(), Font.PLAIN, 24));
        String restartText = "Click anywhere to restart";
        textWidth = g2d.getFontMetrics().stringWidth(restartText);
        x = (getWidth() - textWidth) / 2;
        y = getHeight() / 2 + 50;
        g2d.drawString(restartText, x, y);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bufferedResizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedResizedImage.createGraphics();
        g2d.drawImage(resizedImage, 0, 0, null);
        g2d.dispose();
        return bufferedResizedImage;
    }
}