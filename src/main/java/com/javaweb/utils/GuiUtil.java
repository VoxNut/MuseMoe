package com.javaweb.utils;

import com.javaweb.constant.AppConstant;
import com.javaweb.view.custom.spinner.DateLabelFormatter;
import com.javaweb.view.custom.table.BorderedHeaderRenderer;
import com.javaweb.view.custom.table.BorderedTableCellRenderer;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.SqlDateModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.util.Rotation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;


public class GuiUtil {

    public static void formatTable(JTable table) {
        // Add table styling
        table.setRowHeight(30);
        table.setFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 16));
        table.setForeground(AppConstant.TEXT_COLOR);
        table.setBackground(AppConstant.BACKGROUND_COLOR);
        table.setGridColor(AppConstant.BORDER_COLOR);
        table.setBorder(new LineBorder(AppConstant.BORDER_COLOR));
        table.setFocusable(false);


        // Style the header
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new BorderedHeaderRenderer());
        header.setReorderingAllowed(false);
        header.setBackground(AppConstant.HEADER_BACKGROUND_COLOR);
        header.setForeground(AppConstant.TEXT_COLOR);

        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = 50; // Minimum width
            int maxWidth = 300; // Maximum width

            TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            Component headerComponent = headerRenderer.getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, 0, 0);
            preferredWidth = Math.max(preferredWidth, headerComponent.getPreferredSize().width);

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component cellComponent = table.prepareRenderer(cellRenderer, row, column);
                int cellWidth = cellComponent.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, cellWidth);
            }

            preferredWidth = Math.min(preferredWidth, maxWidth);
            tableColumn.setPreferredWidth(preferredWidth);
        }

        // Add cell renderer
        table.setDefaultRenderer(Object.class, new BorderedTableCellRenderer());
    }

    //    public static JButton createButton(String text) {
//        JButton button = new JButton(text);
//        button.setUI(new CustomButtonUI(AppConstant.BUTTON_BACKGROUND_COLOR, AppConstant.BUTTON_TEXT_COLOR, AppConstant.DISABLE_BACKGROUND_BUTTON, AppConstant.DISABLE_TEXT_BUTTON, AppConstant.DISABLE_BACKGROUND_BUTTON, AppConstant.DISABLE_TEXT_BUTTON));
//        button.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 16));
//        button.setBorderPainted(false);
//        button.setContentAreaFilled(false);
//        button.setFocusPainted(false);
//        button.setOpaque(true);
//        return button;
//    }
    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
        return button;
    }

    public static void styleButton(JButton button, Color bgColor, Color textColor, Color accentColor) {
        button.setBackground(GuiUtil.darkenColor(bgColor, 0.15f));
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(accentColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(GuiUtil.darkenColor(bgColor, 0.15f));
            }
        });
    }

    public static JButton createButton(String text, int size) {
        JButton button = createButton(text);
        button.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, size));
        return button;
    }

    public static JButton createButton(String text, int width, int height) {
        JButton button = createButton(text);
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }

    public static void isDisableButton(JButton button) {
        button.setBackground(AppConstant.DISABLE_BACKGROUND_BUTTON);
        button.setForeground(AppConstant.DISABLE_TEXT_BUTTON);
    }

    public static JButton createButton(String text, int width, int height, float fontSize) {
        JButton button = createButton(text, width, height);
        button.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, fontSize));
        return button;
    }

    public static JLabel createLabel(String text, int style, float size) {
        JLabel label = createLabel(text);
        label.setFont(FontUtil.getJetBrainsMonoFont(style, size));
        return label;
    }

    public static JLabel createLabel(String text, Font font) {
        JLabel label = createLabel(text);
        label.setFont(font);
        return label;
    }

    public static JLabel createSpotifyFontLabel(String text, int style, int size) {
        JLabel label = createLabel(text);
        label.setFont(FontUtil.getSpotifyFont(style, size));
        return label;
    }

    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(AppConstant.TEXT_COLOR);
        return label;
    }

    public static JLabel createLabel() {
        JLabel label = new JLabel();
        label.setForeground(AppConstant.TEXT_COLOR);
        return label;
    }

    public static JTextField createLineInputField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setForeground(AppConstant.TEXT_COLOR);
        textField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstant.TEXT_COLOR)); // Line border
        textField.setBackground(AppConstant.TEXTFIELD_BACKGROUND_COLOR);
        textField.setCaretColor(AppConstant.TEXT_COLOR);
        return textField;
    }

    public static JTextField createLineInputField(String text, int columns) {
        return new JTextField(text, columns);
    }

    public static JTextField createLineInputField(int columns, Dimension textFieldDimension) {
        JTextField textField = createLineInputField(columns);
        textField.setPreferredSize(textFieldDimension);
        return textField;
    }

    public static JTextField createLineInputField(Dimension textFieldDimension) {
        JTextField textField = new JTextField();
        styleInputField(textField, textFieldDimension);
        return textField;
    }

    public static JPasswordField createLineInputPasswordField(Dimension textFieldDimension) {
        JPasswordField textField = new JPasswordField();
        styleInputField(textField, textFieldDimension);
        return textField;
    }

    private static void styleInputField(JTextField textField, Dimension textFieldDimension) {
        textField.setForeground(AppConstant.TEXT_COLOR);
        textField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstant.TEXT_COLOR));
        textField.setBackground(AppConstant.TEXTFIELD_BACKGROUND_COLOR);
        textField.setCaretColor(AppConstant.TEXT_COLOR);
        textField.setPreferredSize(textFieldDimension);
    }


    public static JComboBox<String> createComboBox(String[] contents) {
        JComboBox<String> comboBox = new JComboBox<>(contents);
        comboBox.setBackground(AppConstant.TEXTFIELD_BACKGROUND_COLOR);
        comboBox.setForeground(AppConstant.TEXT_COLOR);
        return comboBox;
    }

    public static JTextArea createTextArea(int rows, int columns) {
        JTextArea textArea = new JTextArea(rows, columns);
        textArea.setForeground(AppConstant.TEXT_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(AppConstant.TEXTFIELD_BACKGROUND_COLOR);
        textArea.setCaretColor(AppConstant.TEXT_COLOR);

        return textArea;
    }

    public static JTextArea createTextArea(int rows, int columns, int width, int height) {
        JTextArea textArea = createTextArea(rows, columns);
        textArea.setMinimumSize(new Dimension(width, height));
        return textArea;
    }


    public static JCheckBox createCheckBox(String content) {
        JCheckBox checkBox = new JCheckBox(content);
        checkBox.setBackground(AppConstant.BACKGROUND_COLOR);
        checkBox.setForeground(AppConstant.TEXT_COLOR);
        checkBox.setFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 14));
        return checkBox;
    }

    public static TitledBorder createTitleBorder(String content) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(content);
        titledBorder.setTitleColor(AppConstant.TEXT_COLOR);

        return titledBorder;
    }


    public static TitledBorder createTitleBorder(String content, int style, int size) {
        TitledBorder titledBorder = createTitleBorder(content);
        titledBorder.setTitleFont(FontUtil.getJetBrainsMonoFont(style, size));
        return titledBorder;
    }


    private static void setComponentBackground(Component component, Color color) {
        component.setBackground(color);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setComponentBackground(child, color);

            }
        }
    }

    public static JMenuItem createMenuItem(String text) {
        return createMenuItem(text, AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR);

    }

    public static JMenuItem createMenuItem(String text, Color backgroundColor, Color textColor) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
        menuItem.setOpaque(true);
        menuItem.setBackground(AppConstant.BACKGROUND_COLOR);
        menuItem.setForeground(AppConstant.TEXT_COLOR);
        menuItem.setMargin(new Insets(0, 0, 0, 0));
        menuItem.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return menuItem;
    }

    public static JMenu createMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 16));
        menu.setOpaque(true);
        menu.setBackground(AppConstant.BACKGROUND_COLOR);
        menu.setForeground(AppConstant.TEXT_COLOR);
        menu.getPopupMenu().setBorder(null);
        menu.setBorderPainted(false);
        return menu;
    }

    public static JSpinner createTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        model.setCalendarField(Calendar.MINUTE);
        JSpinner timeSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(timeSpinner, "HH:mm:ss");
        timeSpinner.setEditor(editor);

        editor.getTextField().setBackground(AppConstant.TEXTFIELD_BACKGROUND_COLOR);
        editor.getTextField().setForeground(AppConstant.TEXT_COLOR);
        editor.getTextField().setFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 16));

        setComponentBackground(timeSpinner, AppConstant.TEXTFIELD_BACKGROUND_COLOR);

        return timeSpinner;
    }

    public static JDatePickerImpl createDatePicker() {
        SqlDateModel model = new SqlDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        datePicker.setOpaque(true);
        datePicker.getJFormattedTextField().setBackground(AppConstant.TEXTFIELD_BACKGROUND_COLOR);
        datePicker.getJFormattedTextField().setForeground(AppConstant.TEXT_COLOR);
        datePicker.getJFormattedTextField().setFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 16));

        setComponentBackground(datePanel, AppConstant.TEXTFIELD_BACKGROUND_COLOR);

        datePanel.setPreferredSize(new Dimension(300, 200));

        // Change the button icon
        for (Component component : datePicker.getComponents()) {
            if (component instanceof JButton button) {
                button.setIcon(GuiUtil.createImageIcon(AppConstant.CALENDAR_PATH, 30, 30));
                button.setText("");
                button.setBorderPainted(false);
                button.setContentAreaFilled(false);
                button.setOpaque(true);
                button.setBackground(AppConstant.TEXTFIELD_BACKGROUND_COLOR);
            }
        }
        datePicker.getJFormattedTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    datePicker.getModel().setSelected(false);
                    datePicker.getJFormattedTextField().setText("");
                }
            }
        });

        return datePicker;
    }


    public static JButton createIconButton(String iconPath, String hoverIconPath) {
        JButton button = new JButton();

        // Set Normal Icon
        if (iconPath != null && !iconPath.isEmpty()) {
            ImageIcon icon = createImageIcon(iconPath, 30, 30);
            if (icon != null) {
                button.setIcon(icon);
            }
        }

        // Set Hover Icon
        if (hoverIconPath != null && !hoverIconPath.isEmpty()) {
            ImageIcon hoverIcon = createImageIcon(hoverIconPath, 30, 30);
            if (hoverIcon != null) {
                button.setRolloverIcon(hoverIcon);
            }
        }

        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        return button;
    }

    public static JButton createIconButton(String iconPath, int width, int height) {
        ImageIcon imageIcon = createImageIcon(iconPath, width, height);
        JButton button = new JButton();
        button.setIcon(imageIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        return button;
    }

    public static JButton changeButtonIconColor(String iconPath, Color color, int width, int height) {
        if (color == null) {
            throw new IllegalArgumentException("Icon path or color cannot be null");
        }

        // Load the original icon
        ImageIcon originalIcon = createImageIcon(iconPath, width, height);

        // Create a copy of the original image to preserve its structure
        BufferedImage originalImg = new BufferedImage(
                originalIcon.getIconWidth(),
                originalIcon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = originalImg.createGraphics();
        originalIcon.paintIcon(null, g, 0, 0);
        g.dispose();

        Color hoverColor = lightenColor(color, 0.3);
        // Create colored versions
        BufferedImage normalColoredImg = applyColorToImage(originalImg, color);
        BufferedImage hoverColoredImg = applyColorToImage(originalImg, hoverColor);

        try {
            ImageIcon normalIcon = new ImageIcon(Thumbnails.of(normalColoredImg).size(width, height).asBufferedImage());
            ImageIcon hoverIcon = new ImageIcon(Thumbnails.of(hoverColoredImg).size(width, height).asBufferedImage());

            JButton newButton = new JButton();
            newButton.setIcon(normalIcon);
            newButton.setRolloverIcon(hoverIcon);
            newButton.setBorderPainted(false);
            newButton.setContentAreaFilled(false);
            newButton.setFocusPainted(false);
            newButton.setOpaque(false);

            return newButton;
        } catch (IOException e) {
            e.printStackTrace();
            return new JButton();
        }
    }

    public static void rotateButtonIcon(JButton button, double angleDegrees) {
        Icon icon = button.getIcon();
        if (icon instanceof ImageIcon imageIcon) {
            Image image = imageIcon.getImage();

            // Convert to BufferedImage
            BufferedImage bufferedImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();

            // Calculate the rotation
            double radians = Math.toRadians(angleDegrees);
            double sin = Math.abs(Math.sin(radians));
            double cos = Math.abs(Math.cos(radians));
            int w = bufferedImage.getWidth();
            int h = bufferedImage.getHeight();
            int newWidth = (int) Math.floor(w * cos + h * sin);
            int newHeight = (int) Math.floor(h * cos + w * sin);

            // Rotate the image
            BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = rotatedImage.createGraphics();
            AffineTransform at = new AffineTransform();
            at.translate((double) newWidth / 2, (double) newHeight / 2);
            at.rotate(radians);
            at.translate((double) -w / 2, (double) -h / 2);
            g2.setTransform(at);
            g2.drawImage(bufferedImage, 0, 0, null);
            g2.dispose();

            // Set the new icon
            button.setIcon(new ImageIcon(rotatedImage));
        }
    }

    public static void rotateLabelIcon(JLabel label, double angleDegrees) {
        Icon icon = label.getIcon();
        if (icon instanceof ImageIcon imageIcon) {
            Image image = imageIcon.getImage();

            // Convert to BufferedImage
            BufferedImage bufferedImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();

            // Calculate the rotation
            double radians = Math.toRadians(angleDegrees);
            double sin = Math.abs(Math.sin(radians));
            double cos = Math.abs(Math.cos(radians));
            int w = bufferedImage.getWidth();
            int h = bufferedImage.getHeight();
            int newWidth = (int) Math.floor(w * cos + h * sin);
            int newHeight = (int) Math.floor(h * cos + w * sin);

            // Rotate the image
            BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = rotatedImage.createGraphics();
            AffineTransform at = new AffineTransform();
            at.translate((double) newWidth / 2, (double) newHeight / 2);
            at.rotate(radians);
            at.translate((double) -w / 2, (double) -h / 2);
            g2.setTransform(at);
            g2.drawImage(bufferedImage, 0, 0, null);
            g2.dispose();

            // Set the new icon
            label.setIcon(new ImageIcon(rotatedImage));
        }
    }

    private static BufferedImage applyColorToImage(BufferedImage original, Color color) {
        BufferedImage tinted = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tinted.createGraphics();

        // Set rendering hints for better quality
        configureGraphicsForHighQuality(g);
        g.drawImage(original, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(color);
        g.fillRect(0, 0, original.getWidth(), original.getHeight());
        g.dispose();

        return tinted;
    }

    public static void changeButtonIconColor(JButton button, Color color) {
        Icon baseIcon = button.getIcon();
        Icon rolloverBaseIcon = button.getRolloverIcon();

        if (baseIcon instanceof ImageIcon baseImageIcon) {
            Image baseImage = baseImageIcon.getImage();
            BufferedImage colored = recolorImage(baseImage, color);
            button.setIcon(new ImageIcon(colored));
        }

        if (rolloverBaseIcon instanceof ImageIcon rolloverImageIcon) {
            Image rolloverImage = rolloverImageIcon.getImage();
            Color hoverColor = lightenColor(color, 0.3f);
            BufferedImage recoloredHover = recolorImage(rolloverImage, hoverColor);
            button.setRolloverIcon(new ImageIcon(recoloredHover));
        }
    }

    private static BufferedImage recolorImage(Image image, Color color) {
        BufferedImage buffered = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffered.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.setComposite(AlphaComposite.SrcAtop);
        g2d.setColor(color);
        g2d.fillRect(0, 0, buffered.getWidth(), buffered.getHeight());
        g2d.dispose();
        return buffered;
    }

    public static void changeLabelIconColor(JLabel label, Color color) {
        Icon icon = label.getIcon();
        if (icon instanceof ImageIcon imageIcon) {
            Image image = imageIcon.getImage();

            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);

            // Apply the color overlay
            g2d.setComposite(AlphaComposite.SrcAtop);
            g2d.setColor(color);
            g2d.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
            g2d.dispose();

            label.setIcon(new ImageIcon(bufferedImage));
        }
    }

    public static void changeIconColor(ImageIcon icon, Color color) {
        // Create a new buffered image with the same dimensions
        BufferedImage img = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);

        // Draw the original icon onto the buffered image
        Graphics2D g = img.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        // Change the color of the icon
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgba = img.getRGB(x, y);
                Color originalColor = new Color(rgba, true);

                // Preserve transparency
                if (originalColor.getAlpha() > 0) {
                    Color newColor = new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            originalColor.getAlpha());
                    img.setRGB(x, y, newColor.getRGB());
                }
            }
        }
        icon.setImage(img);
    }


    public static ImageIcon createRoundedCornerImageIcon(BufferedImage image, int cornerRadius) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Create a new image with transparency (ARGB)
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Prepare the Graphics2D object
        Graphics2D g2 = output.createGraphics();
        configureGraphicsForHighQuality(g2);

        // Draw the original image
        g2.drawImage(image, 0, 0, null);

        // Create the rounded rectangle mask
        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dMask = mask.createGraphics();
        g2dMask.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2dMask.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2dMask.setColor(Color.WHITE);
        g2dMask.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
        g2dMask.dispose();

        // Apply the mask to the image
        g2.setComposite(AlphaComposite.DstIn);
        g2.drawImage(mask, 0, 0, null);

        g2.dispose();

        return new ImageIcon(output);
    }

    public static ImageIcon createImageIcon(String path, int width, int height) {
        ImageIcon imageIcon = new ImageIcon(path);
        try {
            BufferedImage originalImage = ImageIO.read(new File(path));
            BufferedImage resizedImage = Thumbnails.of(originalImage).size(width, height).asBufferedImage();
            imageIcon = new ImageIcon(resizedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageIcon;
    }


    public static BufferedImage createBufferImage(String coverPath) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(coverPath));
            return originalImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Error!");
    }

    public static BufferedImage createBufferImage(BufferedImage bufferedImage, int width, int height) {
        try {
            BufferedImage resizedImage = Thumbnails.of(bufferedImage).size(width, height).asBufferedImage();
            return resizedImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Error!");
    }

    public static ImageIcon createDiscImageIcon(BufferedImage sourceImage, int width, int height, int holeRadius) {
        // High resolution rendering
        int highResWidth = width * 2;
        int highResHeight = height * 2;

        BufferedImage resizedImage = new BufferedImage(highResWidth, highResHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dResize = resizedImage.createGraphics();
        g2dResize.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2dResize.drawImage(sourceImage, 0, 0, highResWidth, highResHeight, null);
        g2dResize.dispose();

        BufferedImage highResDiscImage = new BufferedImage(highResWidth, highResHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dHighRes = highResDiscImage.createGraphics();

        // High-quality rendering hints
        configureGraphicsForHighQuality(g2dHighRes);

        // Draw the circular area
        g2dHighRes.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, highResWidth, highResHeight));
        g2dHighRes.drawImage(resizedImage, 0, 0, null);

        // Cut out the central hole
        g2dHighRes.setComposite(AlphaComposite.Clear);
        int highResHoleRadius = holeRadius * 2;
        int highResCenterX = highResWidth / 2;
        int highResCenterY = highResHeight / 2;
        g2dHighRes.fill(new java.awt.geom.Ellipse2D.Double(highResCenterX - highResHoleRadius, highResCenterY - highResHoleRadius, 2 * highResHoleRadius, 2 * highResHoleRadius));

        g2dHighRes.dispose();

        // Downscale to desired size
        BufferedImage finalDiscImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dFinal = finalDiscImage.createGraphics();
        g2dFinal.drawImage(highResDiscImage, 0, 0, width, height, null);
        g2dFinal.dispose();

        // Return as ImageIcon
        return new ImageIcon(finalDiscImage);
    }

    public static double calculateContrast(Color color1, Color color2) {
        double luminance1 = getLuminance(color1);
        double luminance2 = getLuminance(color2);
        return (Math.max(luminance1, luminance2) + 0.05) / (Math.min(luminance1, luminance2) + 0.05);
    }

    private static double getLuminance(Color color) {
        double r = color.getRed() / 255.0;
        double g = color.getGreen() / 255.0;
        double b = color.getBlue() / 255.0;
        r = (r <= 0.03928) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    public static JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(AppConstant.BACKGROUND_COLOR);
        return panel;
    }

    public static JPanel createPanel(LayoutManager layoutManager) {
        JPanel panel = createPanel();
        panel.setLayout(layoutManager);
        return panel;
    }

    public static JPanel createPanel(TitledBorder title) {
        JPanel panel = createPanel();
        panel.setBorder(title);
        return panel;
    }

    public static JPanel createPanel(LayoutManager layoutManager, TitledBorder title) {
        JPanel panel = createPanel(layoutManager);
        panel.setBorder(title);
        return panel;
    }

    public static JPanel createPanel(LayoutManager layoutManager, Color backgroundColor) {
        JPanel panel = createPanel(layoutManager);
        panel.setBackground(backgroundColor);
        return panel;
    }

    public static JPanel createGradientPanel(LayoutManager layoutManager, Color top, Color bottom) {
        JPanel panel = new GradientPanel(top, bottom);
        panel.setLayout(layoutManager);
        return panel;
    }

    public static void setGradientBackground(JPanel panel, Color topColor, Color bottomColor) {
        panel.setOpaque(false);
        panel.setUI(new javax.swing.plaf.PanelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                int width = c.getWidth();
                int height = c.getHeight();

                // Create a vertical gradient from topColor to bottomColor
                GradientPaint gp = new GradientPaint(0, 0, topColor, 0, height, bottomColor);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();

                super.paint(g, c);
            }
        });
    }

    public static Color getComplementaryColor(Color color) {
        int red = 255 - color.getRed();
        int green = 255 - color.getGreen();
        int blue = 255 - color.getBlue();
        return new Color(red, green, blue);
    }

    public static Color lightenColor(Color color, double fraction) {
        int red = (int) (color.getRed() + (255 - color.getRed()) * fraction);
        int green = (int) (color.getGreen() + (255 - color.getGreen()) * fraction);
        int blue = (int) (color.getBlue() + (255 - color.getBlue()) * fraction);
        return new Color(Math.min(red, 255), Math.min(green, 255), Math.min(blue, 255));
    }

    public static Color darkenColor(Color color, double fraction) {
        int red = (int) Math.max(0, color.getRed() - 255 * fraction);
        int green = (int) Math.max(0, color.getGreen() - 255 * fraction);
        int blue = (int) Math.max(0, color.getBlue() - 255 * fraction);
        return new Color(red, green, blue);
    }


    public static JPopupMenu createPopupMenu(Color backgroundColor, Color textColor) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setBackground(backgroundColor);
        popupMenu.setForeground(textColor);
        popupMenu.setBorder(BorderFactory.createLineBorder(GuiUtil.darkenColor(backgroundColor, 0.2f)));
        return popupMenu;
    }

    public static void styleMenuItem(JMenuItem menuItem, Color bgColor, Color textColor) {
        menuItem.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
        menuItem.setMargin(new Insets(6, 12, 6, 12));
        menuItem.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        menuItem.setBackground(bgColor);
        menuItem.setForeground(textColor);

    }

    public static void setGradientBackground(JPanel panel, Color centerColor, Color outerColor, float centerX, float centerY, float radius) {
        panel.setOpaque(false);
        panel.setUI(new javax.swing.plaf.PanelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                int width = c.getWidth();
                int height = c.getHeight();

                // Enable antialiasing for smoother gradient
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Create center point for gradient
                Point2D center = new Point2D.Float(width * centerX, height * centerY);

                // Create the radial gradient
                RadialGradientPaint gradient = new RadialGradientPaint(center,                    // Center point
                        width * radius,            // Radius
                        new float[]{0.0f, 1.0f},  // Distribution
                        new Color[]{centerColor, outerColor}  // Colors
                );

                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();

                super.paint(g, c);
            }
        });
    }


    public static class GradientPanel extends JPanel {
        private final Color color1;
        private final Color color2;

        public GradientPanel(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            int width = getWidth();
            int height = getHeight();


            GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);

            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
            g2d.dispose();
        }
    }

    public static JFreeChart createPieChart(String title, DefaultPieDataset dataset) {
        // Create chart
        JFreeChart chart = ChartFactory.createPieChart3D(
                title,
                dataset,
                true,
                true,
                false
        );

        chart.getTitle().setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 30));
        chart.getTitle().setPaint(AppConstant.TEXT_COLOR);
        chart.setBackgroundPaint(AppConstant.BACKGROUND_COLOR);
        LegendTitle legend = chart.getLegend();
        legend.setItemFont((FontUtil.getJetBrainsMonoFont(Font.PLAIN, 14)));
        legend.setBackgroundPaint(AppConstant.BACKGROUND_COLOR);
        legend.setItemPaint(AppConstant.TEXT_COLOR);

        //show percentage
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}"));
        plot.setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
        plot.setLabelFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 12));
        plot.setLabelPaint(AppConstant.TEXT_COLOR);
        plot.setLabelBackgroundPaint(AppConstant.BACKGROUND_COLOR);


        // Set 3D properties
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(1.0f);
        plot.setBackgroundPaint(AppConstant.BACKGROUND_COLOR);
        plot.setOutlineVisible(false);
        plot.setSectionOutlinesVisible(false);

        return chart;
    }

    public static JFreeChart createBarChart(String title, String xAxisLabel, String yAxisLabel, DefaultCategoryDataset dataset, PlotOrientation orientation) {
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                orientation,
                true, true, false);

        chart.getTitle().setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 30));
        chart.getTitle().setPaint(AppConstant.TEXT_COLOR);
        chart.setBackgroundPaint(AppConstant.BACKGROUND_COLOR);

        LegendTitle legend = chart.getLegend();
        legend.setItemFont((FontUtil.getJetBrainsMonoFont(Font.PLAIN, 14)));
        legend.setBackgroundPaint(AppConstant.BACKGROUND_COLOR);
        legend.setItemPaint(AppConstant.TEXT_COLOR);


        // Customize the plot
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(AppConstant.BACKGROUND_COLOR);
        plot.setRangeGridlinePaint(AppConstant.TEXT_COLOR);
        plot.setOutlineVisible(false);

        // Customize the renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false); // Disable shadows
        renderer.setSeriesPaint(0, AppConstant.TEXT_COLOR);


        // Customize the domain axis (x-axis)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(AppConstant.TEXT_COLOR);
        domainAxis.setLabelPaint(AppConstant.TEXT_COLOR);
        domainAxis.setLabelFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 14));
        domainAxis.setTickLabelFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 12));


        // Customize the range axis (y-axis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(AppConstant.TEXT_COLOR);
        rangeAxis.setLabelPaint(AppConstant.TEXT_COLOR);
        rangeAxis.setLabelFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 14));
        rangeAxis.setTickLabelFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 12));


        return chart;
    }

    public static JFreeChart createLineChart(String title, String xAxisLabel, String yAxisLabel, DefaultCategoryDataset dataset, PlotOrientation orientation) {
        JFreeChart chart = ChartFactory.createLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                orientation,
                true,
                true,
                false
        );

        // Customize chart appearance
        chart.getTitle().setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 30));
        chart.getTitle().setPaint(AppConstant.TEXT_COLOR);
        chart.setBackgroundPaint(AppConstant.BACKGROUND_COLOR);

        LegendTitle legend = chart.getLegend();
        legend.setItemFont((FontUtil.getJetBrainsMonoFont(Font.PLAIN, 14)));
        legend.setBackgroundPaint(AppConstant.BACKGROUND_COLOR);
        legend.setItemPaint(AppConstant.TEXT_COLOR);


        CategoryPlot plot = chart.getCategoryPlot();
        plot.setOutlineVisible(false);
        plot.setBackgroundPaint(AppConstant.BACKGROUND_COLOR);
        plot.setRangeGridlinePaint(AppConstant.TEXT_COLOR);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(AppConstant.TEXT_COLOR);

        // Customize the domain axis (x-axis)
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(AppConstant.TEXT_COLOR);
        domainAxis.setLabelPaint(AppConstant.TEXT_COLOR);
        domainAxis.setLabelFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 14));
        domainAxis.setTickLabelFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 12));


        // Customize the range axis (y-axis)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelPaint(AppConstant.TEXT_COLOR);
        rangeAxis.setLabelPaint(AppConstant.TEXT_COLOR);
        rangeAxis.setLabelFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 14));
        rangeAxis.setTickLabelFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 12));

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, AppConstant.TEXT_COLOR);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

        return chart;
    }

    public static void showMessageDialog(Component parentComponent, String message, String title, int messageType) {
        JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
    }

    public static void showNetworkErrorDialog(Component parentComponent, String message) {
        Color errorColor = new Color(231, 76, 60);
        showCustomMessageDialog(parentComponent,
                "Network Error: " + message + "\nPlease check your internet connection and try again."
                , "Error", JOptionPane.ERROR_MESSAGE,
                AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, errorColor);
    }

    public static void showErrorMessageDialog(Component parentComponent, String message) {
        Color errorColor = new Color(231, 76, 60); // A more refined red
        showCustomMessageDialog(parentComponent, message, "Error", JOptionPane.ERROR_MESSAGE,
                AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, errorColor);
    }

    public static void showSuccessMessageDialog(Component parentComponent, String message) {
        Color successColor = new Color(46, 204, 113); // Emerald green
        showCustomMessageDialog(parentComponent, message, "Success", JOptionPane.PLAIN_MESSAGE,
                AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, successColor);
    }

    public static void showWarningMessageDialog(Component parentComponent, String message) {
        Color warningColor = new Color(241, 196, 15); // Vibrant yellow
        showCustomMessageDialog(parentComponent, message, "Warning", JOptionPane.WARNING_MESSAGE,
                AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, warningColor);
    }

    public static void showInfoMessageDialog(Component parentComponent, String message) {
        Color infoColor = new Color(52, 152, 219); // Light blue
        showCustomMessageDialog(parentComponent, message, "Information", JOptionPane.INFORMATION_MESSAGE,
                AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, infoColor);
    }

    public static int showCustomConfirmDialog(Component parent, String message, String title,
                                              Color bgColor, Color textColor, Color accentColor) {
        final int[] result = new int[1];
        result[0] = JOptionPane.CLOSED_OPTION;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        setGradientBackground(mainPanel, bgColor, darkenColor(bgColor, 0.2f), 0.5f, 0.5f, 0.5f);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        styleDialog(dialog, bgColor, textColor);

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
        messageLabel.setForeground(textColor);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create custom icon
        Icon icon = createCustomDialogIcon(JOptionPane.QUESTION_MESSAGE, 40, accentColor, bgColor);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setVerticalAlignment(SwingConstants.TOP);

        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(messageLabel, BorderLayout.CENTER);

        JButton yesButton = new JButton("Yes");
        JButton noButton = new JButton("No");

        // Style buttons
        for (JButton button : new JButton[]{yesButton, noButton}) {
            button.setBackground(accentColor);
            button.setForeground(textColor);
            button.setBorderPainted(false);
            button.setContentAreaFilled(true);
            button.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));

            // Add hover effect
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(darkenColor(accentColor, 0.2f));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(accentColor);
                }
            });
        }

        yesButton.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });

        noButton.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    public static int showConfirmMessageDialog(Component parentComponent, String message, String title) {
        return showCustomConfirmDialog(parentComponent, message, title,
                AppConstant.BACKGROUND_COLOR, AppConstant.TEXT_COLOR, new Color(52, 152, 219));
    }


    public static BufferedImage createDefaultAvatar(int width, int height) {
        BufferedImage avatar = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = avatar.createGraphics();
        configureGraphicsForHighQuality(g2);

        // Create a gradient background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(100, 100, 100),
                width, height, new Color(50, 50, 50));
        g2.setPaint(gradient);
        g2.fillOval(0, 0, width, height);

        // Add a user silhouette
        g2.setColor(new Color(200, 200, 200, 180));
        int headSize = width / 3;
        g2.fillOval(width / 2 - headSize / 2, height / 3 - headSize / 2, headSize, headSize);

        // Draw body
        g2.fillOval(width / 2 - width / 4, height / 2, width / 2, height / 3);

        g2.dispose();
        return avatar;
    }


    public static void configureGraphicsForHighQuality(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    }

    public static BufferedImage createSmoothCircularAvatar(BufferedImage sourceImage, int diameter) {
        if (sourceImage == null) {
            return createDefaultAvatar(diameter, diameter);
        }

        int highResDiameter = diameter * 3;

        try {
            BufferedImage highResImage = Thumbnails.of(sourceImage)
                    .size(highResDiameter, highResDiameter)
                    .crop(Positions.CENTER)
                    .antialiasing(Antialiasing.ON)
                    .outputQuality(1.0f)
                    .asBufferedImage();

            // Create a high-resolution output image
            BufferedImage highResOutput = new BufferedImage(
                    highResDiameter, highResDiameter, BufferedImage.TYPE_INT_ARGB);

            // Setup high-quality rendering
            Graphics2D g2d = highResOutput.createGraphics();

            // Apply professional rendering settings
            configureGraphicsForHighQuality(g2d);

            // Create circular clip shape
            Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, highResDiameter, highResDiameter);
            g2d.setClip(circle);

            // Draw the image
            g2d.drawImage(highResImage, 0, 0, null);

            // Add a subtle border for better definition
            g2d.setClip(null);
            g2d.setColor(new Color(0, 0, 0, 50));  // More visible but still subtle border
            g2d.setStroke(new BasicStroke(3.0f));  // Thicker border at high res
            g2d.draw(circle);

            // Add a subtle highlight for 3D effect (optional)
            g2d.setClip(circle);
            g2d.setComposite(AlphaComposite.SrcAtop.derive(0.1f));
            g2d.setPaint(new GradientPaint(
                    0, 0, Color.WHITE,
                    0, highResDiameter, new Color(255, 255, 255, 0)));
            g2d.fillRect(0, 0, highResDiameter, highResDiameter);

            g2d.dispose();

            // Scale back down to the requested size with high quality
            BufferedImage finalImage = Thumbnails.of(highResOutput)
                    .size(diameter, diameter)
                    .antialiasing(Antialiasing.ON)
                    .outputQuality(1.0f)
                    .asBufferedImage();

            return finalImage;
        } catch (IOException e) {
            e.printStackTrace();

            // Fallback to simpler method if there's an error
            return createDefaultAvatar(diameter, diameter);
        }
    }

    public static void styleDialog(JDialog dialog, Color backgroundColor, Color textColor) {
        dialog.getRootPane().putClientProperty("TitlePane.font", FontUtil.getSpotifyFont(Font.BOLD, 16));
        dialog.getRootPane().putClientProperty("JRootPane.titleBarBackground", backgroundColor);
        dialog.getRootPane().putClientProperty("JRootPane.titleBarForeground", textColor);
        dialog.getRootPane().putClientProperty("JRootPane.titleBarInactiveBackground", backgroundColor);
        dialog.getRootPane().putClientProperty("JRootPane.titleBarInactiveForeground", textColor);

        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder());

        // Apply the changes
        SwingUtilities.updateComponentTreeUI(dialog);
    }

    public static void styleTitleBar(JFrame frame, Color backgroundColor, Color textColor, int fontSize) {
        // Create a slightly darker background for the title bar
        Color titleBarBackground = darkenColor(backgroundColor, 0.1f);

        // Set title bar font
        frame.getRootPane().putClientProperty("TitlePane.font", FontUtil.getSpotifyFont(Font.BOLD, fontSize));

        // Set title bar colors
        frame.getRootPane().putClientProperty("JRootPane.titleBarBackground", titleBarBackground);
        frame.getRootPane().putClientProperty("JRootPane.titleBarForeground", textColor);

        // Set inactive state colors
        frame.getRootPane().putClientProperty("JRootPane.titleBarInactiveBackground", titleBarBackground);
        frame.getRootPane().putClientProperty("JRootPane.titleBarInactiveForeground", darkenColor(textColor, 0.2f));

        // Apply the changes to the frame
        SwingUtilities.updateComponentTreeUI(frame.getContentPane());
    }

    public static void styleTitleBar(JFrame frame, Color backgroundColor, Color textColor) {
        styleTitleBar(frame, backgroundColor, textColor, 18);
    }

    public static void setGradientBackground(JToolBar toolBar, Color centerColor, Color outerColor,
                                             float centerX, float centerY, float radius) {
        toolBar.setOpaque(false);

        toolBar.setUI(new javax.swing.plaf.basic.BasicToolBarUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                paintGradient(g, c, centerColor, outerColor, centerX, centerY, radius);
                super.paint(g, c);
            }
        });

        toolBar.setBorderPainted(false);
        toolBar.setFloatable(false);
    }

    /**
     * Helper method to paint gradient on a component
     */
    private static void paintGradient(Graphics g, JComponent c, Color centerColor, Color outerColor,
                                      float centerX, float centerY, float radius) {
        Graphics2D g2d = (Graphics2D) g.create();
        int width = c.getWidth();
        int height = c.getHeight();

        // Enable antialiasing for smoother gradient
        configureGraphicsForHighQuality(g2d);

        // Create center point for gradient
        Point2D center = new Point2D.Float(width * centerX, height * centerY);

        // Create the radial gradient
        RadialGradientPaint gradient = new RadialGradientPaint(
                center,                    // Center point
                width * radius,            // Radius
                new float[]{0.0f, 1.0f},  // Distribution
                new Color[]{centerColor, outerColor}  // Colors
        );

        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
    }


    public static JDialog createCustomMessageDialog(Component parent, String message, String title, int messageType,
                                                    Color bgColor, Color textColor, Color accentColor) {
        // Create a custom JDialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);

        // Create main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        setGradientBackground(mainPanel, bgColor, darkenColor(bgColor, 0.2f), 0.5f, 0.5f, 0.5f);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add title bar styling
        styleDialog(dialog, bgColor, textColor);

        // Create message label with proper styling
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
        messageLabel.setForeground(textColor);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create icon based on message type
        String iconPath = switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> AppConstant.ERROR_ICON_PATH;
            case JOptionPane.WARNING_MESSAGE -> AppConstant.WARNING_ICON_PATH;
            case JOptionPane.INFORMATION_MESSAGE -> AppConstant.INFORMATION_ICON_PATH;
            default -> AppConstant.SUCCESS_ICON_PATH;
        };

        Icon icon = createColoredDialogIcon(iconPath, 40, accentColor);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setVerticalAlignment(SwingConstants.TOP);

        // Add icon and message to panel
        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(messageLabel, BorderLayout.CENTER);

        // Create and style OK button
        JButton okButton = new JButton("OK");
        okButton.setBackground(accentColor);
        okButton.setForeground(textColor);
        okButton.setBorderPainted(false);
        okButton.setContentAreaFilled(true);
        okButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        okButton.addActionListener(e -> dialog.dispose());

        // Add hover effect
        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                okButton.setBackground(darkenColor(accentColor, 0.2f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                okButton.setBackground(accentColor);
            }
        });

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set dialog content
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        return dialog;
    }

    public static void showCustomMessageDialog(Component parent, String message, String title, int messageType,
                                               Color bgColor, Color textColor, Color accentColor) {
        JDialog dialog = createCustomMessageDialog(parent, message, title, messageType, bgColor, textColor, accentColor);
        dialog.setVisible(true);
    }

    public static Icon createCustomDialogIcon(int type, int size, Color primaryColor, Color backgroundColor) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        configureGraphicsForHighQuality(g2d);

        int padding = size / 6;

        // Draw circular background
        g2d.setColor(backgroundColor);
        g2d.fillOval(0, 0, size, size);

        // Choose icon style based on type
        switch (type) {
            case JOptionPane.ERROR_MESSAGE:
                // Draw X mark
                g2d.setColor(primaryColor);
                g2d.setStroke(new BasicStroke(size / 8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(padding, padding, size - padding, size - padding);
                g2d.drawLine(size - padding, padding, padding, size - padding);
                break;

            case JOptionPane.WARNING_MESSAGE:
                // Draw exclamation mark
                g2d.setColor(primaryColor);

                // Triangle
                int[] xPoints = {size / 2, size - padding, padding};
                int[] yPoints = {padding, size - padding, size - padding};
                g2d.fillPolygon(xPoints, yPoints, 3);

                // Exclamation dot
                g2d.setColor(backgroundColor);
                g2d.fillOval(size / 2 - size / 10, size - padding - size / 6, size / 5, size / 5);

                // Exclamation line
                g2d.fillRoundRect(size / 2 - size / 12, padding + size / 5,
                        size / 6, size / 2, size / 10, size / 10);
                break;

            case JOptionPane.INFORMATION_MESSAGE:
                // Draw info icon (i)
                g2d.setColor(primaryColor);

                // Draw dot
                g2d.fillOval(size / 2 - size / 10, padding + size / 5, size / 5, size / 5);

                // Draw stem
                g2d.fillRoundRect(size / 2 - size / 12, padding + size / 2,
                        size / 6, size / 3, size / 10, size / 10);
                break;

            case JOptionPane.QUESTION_MESSAGE:
                // Draw question mark
                g2d.setColor(primaryColor);
                Font font = FontUtil.getSpotifyFont(Font.BOLD, (float) (size * 3) / 4);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                String text = "?";
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();
                g2d.drawString(text, (size - textWidth) / 2, (size + textHeight) / 2 - fm.getDescent() / 2);
                break;

            default: // Plain message or custom
                // Draw checkmark for success
                g2d.setColor(primaryColor);
                g2d.setStroke(new BasicStroke(size / 8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] checkX = {padding, size / 5 * 2, size - padding};
                int[] checkY = {size / 2, size - padding / 2, padding};
                g2d.drawPolyline(checkX, checkY, 3);
                break;
        }

        g2d.dispose();
        return new ImageIcon(image);
    }

    public static Icon createColoredDialogIcon(String iconPath, int size, Color color) {
        try {
            // Load the original icon
            ImageIcon originalIcon = createImageIcon(iconPath, size, size);
            changeIconColor(originalIcon, color);
            return originalIcon;
        } catch (Exception e) {
            e.printStackTrace();
            return createCustomDialogIcon(getMessageTypeForPath(iconPath), size, color,
                    AppConstant.BACKGROUND_COLOR);
        }
    }

    private static int getMessageTypeForPath(String iconPath) {
        return switch (iconPath) {
            case AppConstant.ERROR_ICON_PATH -> JOptionPane.ERROR_MESSAGE;
            case AppConstant.WARNING_ICON_PATH -> JOptionPane.WARNING_MESSAGE;
            case AppConstant.INFORMATION_ICON_PATH -> JOptionPane.INFORMATION_MESSAGE;
            default -> JOptionPane.PLAIN_MESSAGE;
        };
    }

}
