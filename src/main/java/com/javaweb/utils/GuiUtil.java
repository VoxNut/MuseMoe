package com.javaweb.utils;

import com.javaweb.constant.AppConstant;
import com.javaweb.model.dto.ArtistDTO;
import com.javaweb.model.dto.UserDTO;
import com.javaweb.view.custom.spinner.DateLabelFormatter;
import com.javaweb.view.custom.table.BorderedHeaderRenderer;
import com.javaweb.view.custom.table.BorderedTableCellRenderer;
import com.javaweb.view.panel.ExpandableCardPanel;
import com.javaweb.view.theme.ThemeManager;
import de.androidpit.colorthief.ColorThief;
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
import javax.swing.border.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class GuiUtil {

    private static final Map<String, BufferedImage> imageCache = new ConcurrentHashMap<>();
    private static final Map<String, Future<BufferedImage>> processingImageCache = new ConcurrentHashMap<>();
    private static final ExecutorService imageProcessingExecutor =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "ImageProcessingThread");
                t.setDaemon(true);
                return t;
            });

    public static void formatTable(JTable table) {
        // Add table styling
        table.setRowHeight(30);
        table.setFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 16));
        table.setForeground(ThemeManager.getInstance().getTextColor());
        table.setBackground(ThemeManager.getInstance().getBackgroundColor());
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

    public static void disableJaudiotaggerLogging() {
        Logger rootLogger = Logger.getLogger("");

        rootLogger.setLevel(Level.WARNING);

        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.WARNING);
        }

        Logger jaudiotaggerLogger = Logger.getLogger("org.jaudiotagger");
        jaudiotaggerLogger.setLevel(Level.SEVERE);
    }


    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
        button.setForeground(ThemeManager.getInstance().getTextColor());

        button.putClientProperty("JButton.focusedBackground", button.getBackground());
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIconTextGap(8);
        button.setMargin(new Insets(5, 10, 5, 10));

        // Default background
        Color baseColor = GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f);
        Color hoverColor = GuiUtil.darkenColor(baseColor, 0.15f);
        Color pressColor = GuiUtil.darkenColor(baseColor, 0.25f);

        button.setBackground(baseColor);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(pressColor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.getBounds().contains(e.getPoint())) {
                    button.setBackground(hoverColor);
                } else {
                    button.setBackground(baseColor);
                }
            }
        });

        return button;
    }

    public static JButton createIconButtonWithText(String text, String iconPath) {
        JButton button = createButton(text);
        button.setIcon(GuiUtil.createColoredIcon(iconPath, 16));
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


    public static JLabel createLabel(String text, Font font) {
        JLabel label = createLabel(text);
        label.setFont(font);
        return label;
    }


    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ThemeManager.getInstance().getTextColor());
        return label;
    }

    public static JLabel createLabel(String text, int style, float size) {
        JLabel label = createLabel(text);
        label.setFont(FontUtil.getJetBrainsMonoFont(style, size));
        return label;
    }

    public static JLabel createLabel() {
        JLabel label = new JLabel();
        label.setForeground(ThemeManager.getInstance().getTextColor());
        return label;
    }

    public static JTextField createLineInputField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setForeground(AppConstant.TEXT_COLOR);
        textField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppConstant.TEXT_COLOR));
        textField.setBackground(AppConstant.TEXTFIELD_BACKGROUND_COLOR);
        textField.setCaretColor(AppConstant.TEXT_COLOR);
        return textField;
    }

    public static JTextField createInputField(String text, int columns) {
        JTextField textField = new JTextField(text, columns);
        textField.setOpaque(false);
        textField.setForeground(ThemeManager.getInstance().getTextColor());
        textField.setBackground(ThemeManager.getInstance().getBackgroundColor());
        textField.setCaretColor(ThemeManager.getInstance().getTextColor());
        return textField;
    }

    public static CompoundBorder createCompoundBorder(int thicc) {
        return createCompoundBorder(ThemeManager.getInstance().getTextColor(), thicc, 5, 0, 5, 0);
    }

    public static CompoundBorder createCompoundBorder(Color textColor, int thicc, int top, int left, int bottom, int right) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(textColor, thicc, true),
                BorderFactory.createEmptyBorder(top, left, bottom, right));
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
        JMenuItem menuItem = new JMenuItem(text);
        Color backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        Color textColor = ThemeManager.getInstance().getTextColor();

        menuItem.setOpaque(true);
        menuItem.setBackground(backgroundColor);
        menuItem.setForeground(textColor);

        // Add hover effect
        Color hoverColor = lightenColor(backgroundColor, 0.1f);
        menuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menuItem.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                menuItem.setBackground(backgroundColor);
            }
        });

        return menuItem;
    }


    public static void applyModernScrollBar(Component component) {
        Color backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        Color textColor = ThemeManager.getInstance().getTextColor();
        Color accentColor = ThemeManager.getInstance().getAccentColor();
        if (component instanceof JScrollPane sp) {
            sp.setBorder(BorderFactory.createEmptyBorder());
            sp.getViewport().setBackground(backgroundColor);

            // Style scrollbars
            sp.getVerticalScrollBar().setBackground(backgroundColor);
            sp.getVerticalScrollBar().setForeground(textColor);
            sp.getHorizontalScrollBar().setBackground(backgroundColor);
            sp.getHorizontalScrollBar().setForeground(accentColor);
        }
    }


    public static JMenu createMenu(String text) {
        JMenu menu = new JMenu(text);
        Color backgroundColor = ThemeManager.getInstance().getBackgroundColor();
        Color textColor = ThemeManager.getInstance().getTextColor();

        menu.setOpaque(true);
        menu.setBackground(backgroundColor);
        menu.setForeground(textColor);

        // Style the popup menu
        JPopupMenu popupMenu = menu.getPopupMenu();
        popupMenu.setBackground(backgroundColor);
        popupMenu.setForeground(textColor);
        popupMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(darkenColor(backgroundColor, 0.2f), 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        return menu;
    }

    public static void styleToolBar(JToolBar toolBar, Color backgroundColor, Color textColor) {
        if (toolBar == null) return;

        toolBar.setOpaque(true);
        toolBar.setBorderPainted(false);
        toolBar.setBackground(backgroundColor);
        toolBar.setForeground(textColor);

        // Also update the components inside the toolbar
        for (Component comp : toolBar.getComponents()) {
            if (comp instanceof JMenuBar) {
                styleMenuBar((JMenuBar) comp, backgroundColor, textColor);
            }
        }
    }

    public static void styleMenuBar(JMenuBar menuBar, Color backgroundColor, Color textColor) {
        if (menuBar == null) return;

        // Style the menu bar itself
        menuBar.setOpaque(true);
        menuBar.setBorderPainted(false);
        menuBar.setBackground(backgroundColor);
        menuBar.setForeground(textColor);
        menuBar.setFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 16));

        // Style each menu in the menu bar
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu != null) {
                styleMenu(menu, backgroundColor, textColor);
            }
        }
    }

    public static void styleMenu(JMenu menu, Color backgroundColor, Color textColor) {
        if (menu == null) return;

        // Style the menu itself
        menu.setOpaque(true);
        menu.setBackground(backgroundColor);
        menu.setForeground(textColor);
        menu.setFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 16));

        // Get the popup menu
        JPopupMenu popupMenu = menu.getPopupMenu();
        popupMenu.setBackground(backgroundColor);
        popupMenu.setForeground(textColor);

        // Apply a subtle border to the popup
        popupMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(darkenColor(backgroundColor, 0.2f), 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        // Style all menu items
        for (Component comp : popupMenu.getComponents()) {
            if (comp instanceof JMenuItem menuItem) {
                styleMenuItem(menuItem, backgroundColor, textColor);
            }
        }
    }

    public static void styleMenuItem(JMenuItem menuItem, Color backgroundColor, Color textColor) {
        if (menuItem == null) return;

        // Basic styling
        menuItem.setOpaque(true);
        menuItem.setBackground(backgroundColor);
        menuItem.setForeground(textColor);
        menuItem.setFont(FontUtil.getJetBrainsMonoFont(Font.PLAIN, 16));

        // Style submenu if this menu item is a JMenu
        if (menuItem instanceof JMenu) {
            styleMenu((JMenu) menuItem, backgroundColor, textColor);
        }

        // Add hover effect
        Color hoverColor = lightenColor(backgroundColor, 0.1f);
        menuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menuItem.setBackground(hoverColor);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                menuItem.setBackground(backgroundColor);
            }
        });

        // Update accelerator text color if present
        if (menuItem.getAccelerator() != null) {
            UIManager.put("MenuItem.acceleratorForeground", textColor);
        }
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

    public static JButton changeButtonIconColor(String iconPath, int width, int height) {
        if (ThemeManager.getInstance().getTextColor() == null) {
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

        Color hoverColor = lightenColor(ThemeManager.getInstance().getTextColor(), 0.3);
        // Create colored versions
        BufferedImage normalColoredImg = applyColorToImage(originalImg, ThemeManager.getInstance().getTextColor());
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
            newButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return newButton;
        } catch (IOException e) {
            e.printStackTrace();
            return new JButton();
        }
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
        BufferedImage normalColoredImg = applyColorToImage(originalImg, ThemeManager.getInstance().getTextColor());
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
            newButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

    public static void changeButtonIconColor(JButton button) {
        Icon baseIcon = button.getIcon();
        Icon rolloverBaseIcon = button.getRolloverIcon();

        if (baseIcon instanceof ImageIcon baseImageIcon) {
            Image baseImage = baseImageIcon.getImage();
            BufferedImage colored = recolorImage(baseImage, ThemeManager.getInstance().getTextColor());
            button.setIcon(new ImageIcon(colored));
        }

        if (rolloverBaseIcon instanceof ImageIcon rolloverImageIcon) {
            Image rolloverImage = rolloverImageIcon.getImage();
            Color hoverColor = lightenColor(ThemeManager.getInstance().getTextColor(), 0.3f);
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

    public static void changeLabelIconColor(JLabel label) {
        Icon icon = label.getIcon();
        if (icon instanceof ImageIcon imageIcon) {
            Image image = imageIcon.getImage();

            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);

            // Apply the color overlay
            g2d.setComposite(AlphaComposite.SrcAtop);
            g2d.setColor(ThemeManager.getInstance().getTextColor());
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


    public static JPanel createGradientHeartPanel(int width, int height, int cornerRadius, int iconSize) {
        JPanel heartIconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                configureGraphicsForHighQuality(g2d);

                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(0xE8128A),
                        getWidth(), getHeight(), new Color(0x26C6DA)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

                // Draw heart icon
                Icon heartIcon = createColoredIcon(AppConstant.HEART_ICON_PATH, Color.WHITE, iconSize, iconSize);
                heartIcon.paintIcon(this, g2d, (getWidth() - iconSize) / 2, (getHeight() - iconSize) / 2);

                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width, height);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(width, height);
            }
        };

        heartIconPanel.setOpaque(false);
        return heartIconPanel;
    }


    public static BufferedImage createRoundedCornerImage(BufferedImage image, int cornerRadius, int width, int height) {
        if (image == null) return null;

        // Create a cache key based on image hashcode and dimensions
        String cacheKey = image.hashCode() + "-" + width + "-" + height + "-" + cornerRadius;

        // Check if we have this image already processed
        if (imageCache.containsKey(cacheKey)) {
            return imageCache.get(cacheKey);
        }

        try {
            // First, use Thumbnailator for high-quality resizing
            BufferedImage resized = Thumbnails.of(image)
                    .size(width, height)
                    .keepAspectRatio(true)
                    .crop(Positions.CENTER)
                    .antialiasing(Antialiasing.ON)
                    .outputQuality(1.0f)
                    .asBufferedImage();

            // Create an output image with transparency
            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = output.createGraphics();

            // Enable high quality rendering
            configureGraphicsForHighQuality(g2);

            // Calculate safe corner radius
            int safeRadius = Math.min(cornerRadius, Math.min(width, height) / 2);

            // Create rounded rectangle clip
            g2.setClip(new RoundRectangle2D.Float(0, 0, width, height, safeRadius, safeRadius));

            // Draw the resized image
            g2.drawImage(resized, 0, 0, width, height, null);

            // Add a subtle shadow/border for better definition
            g2.setClip(null);
            g2.setColor(new Color(0, 0, 0, 30));
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0, 0, width, height, safeRadius, safeRadius));

            g2.dispose();

            // Cache the processed image
            imageCache.put(cacheKey, output);

            return output;
        } catch (Exception e) {
            e.printStackTrace();

            // Fallback to simple scaling if Thumbnailator fails
            try {
                BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = output.createGraphics();

                configureGraphicsForHighQuality(g2);

                int safeRadius = Math.min(cornerRadius, Math.min(width, height) / 2);
                g2.setClip(new RoundRectangle2D.Float(0, 0, width, height, safeRadius, safeRadius));

                g2.drawImage(image, 0, 0, width, height, null);
                g2.dispose();

                // Cache the fallback image
                imageCache.put(cacheKey, output);

                return output;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public static ImageIcon createRoundedCornerImageIcon(BufferedImage image, int cornerRadius, int width, int height) {
        return new ImageIcon(createRoundedCornerImage(image, cornerRadius, width, height));
    }

    public static JLabel createRoundedCornerImageLabel(BufferedImage image, int cornerRadius, int width, int height) {
        JLabel label = new JLabel();
        label.setIcon(createRoundedCornerImageIcon(image, cornerRadius, width, height));
        return label;
    }

    public static JLabel createRoundedCornerImageLabel(String path, int cornerRadius, int width, int height) {
        JLabel label = new JLabel();
        label.setIcon(createRoundedCornerImageIcon(path, cornerRadius, width, height));
        return label;
    }


    public static ImageIcon createRoundedCornerImageIcon(String path, int cornerRadius, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(path));
            return createRoundedCornerImageIcon(originalImage, cornerRadius, width, height);
        } catch (IOException e) {
            e.printStackTrace();
            return new ImageIcon(path);
        }
    }


    public static JDialog createStyledDialog(Frame owner, String title, JPanel contentPanel,
                                             Color backgroundColor, Color textColor) {
        JDialog dialog = new JDialog(owner, title, true);
        dialog.setContentPane(contentPanel);

        // Style the dialog
        styleDialog(dialog, backgroundColor, textColor);

        // Size and position
        dialog.pack();
        dialog.setLocationRelativeTo(owner);

        return dialog;
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
        panel.setOpaque(false);
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

    public static ImageIcon createColoredIcon(String path, Color color, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(path));
            BufferedImage coloredImage = applyColorToImage(originalImage, color);
            return new ImageIcon(Thumbnails.of(coloredImage).size(width, height).asBufferedImage());
        } catch (IOException e) {
            e.printStackTrace();
            return new ImageIcon();
        }
    }

    private static Icon createPlaceholderIcon(int size) {
        BufferedImage placeholder = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        g.setColor(Color.PINK);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLACK);
        g.drawString("?", size / 3, size / 2); // simple fallback
        g.dispose();
        return new ImageIcon(placeholder);
    }

    public static <T> java.util.List<Component> findComponentsByType(Container container, Class<T> type) {
        java.util.List<Component> components = new ArrayList<>();

        for (Component component : container.getComponents()) {
            if (type.isInstance(component)) {
                components.add(component);
            }

            if (component instanceof Container) {
                components.addAll(findComponentsByType((Container) component, type));
            }
        }

        return components;
    }

    public static JLabel createPlaylistIconLabel(int width, int height, Color backgroundColor, Color foregroundColor) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing for smoother graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded rectangle background
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, width, height, 6, 6);

        // Draw lines to suggest playlist content
        g2d.setColor(foregroundColor);
        int lineWidth = (int) (width * 0.7);
        int lineHeight = height / 10;
        int lineX = (width - lineWidth) / 2;
        int startY = height / 4;

        for (int i = 0; i < 3; i++) {
            g2d.fillRoundRect(lineX, startY + i * (lineHeight + 2), lineWidth, lineHeight, 2, 2);
        }

        g2d.dispose();

        return new JLabel(new ImageIcon(image));
    }

    public static TitledBorder createTitledBorder(String title, int justification) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeManager.getInstance().getTextColor(), 2, true),
                title,
                justification,
                TitledBorder.TOP,
                FontUtil.getSpotifyFont(Font.BOLD, 14),
                ThemeManager.getInstance().getTextColor());
    }

    public static void updatePanelColors(Container container, Color backgroundColor, Color textColor, Color accentColor) {
        if (container == null) return;

        // Update the container's own properties if it's a JComponent
        if (container instanceof JComponent jComponent) {

            // Handle different types of borders
            if (jComponent.getBorder() instanceof TitledBorder titledBorder) {
                titledBorder.setTitleColor(textColor);
                // Also update the border color itself, not just the title
                if (titledBorder.getBorder() instanceof LineBorder) {
                    titledBorder.setBorder(BorderFactory.createLineBorder(textColor, 2, true));
                }
            } else if (jComponent.getBorder() instanceof LineBorder) {
                // Update plain line borders
                jComponent.setBorder(BorderFactory.createLineBorder(textColor,
                        ((LineBorder) jComponent.getBorder()).getThickness(),
                        ((LineBorder) jComponent.getBorder()).getRoundedCorners()));
            } else if (jComponent.getBorder() instanceof CompoundBorder compoundBorder) {
                if (compoundBorder.getOutsideBorder() instanceof LineBorder) {
                    Border insideBorder = compoundBorder.getInsideBorder();
                    jComponent.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(textColor,
                                    ((LineBorder) compoundBorder.getOutsideBorder()).getThickness(),
                                    ((LineBorder) compoundBorder.getOutsideBorder()).getRoundedCorners()),
                            insideBorder
                    ));
                }
            } else if (jComponent.getBorder() instanceof MatteBorder matteBorder) {
                Insets insets = matteBorder.getBorderInsets();
                jComponent.setBorder(BorderFactory.createMatteBorder(
                        insets.top, insets.left, insets.bottom, insets.right, textColor));
            }
        }

        if (container instanceof JScrollPane scrollPane) {
            applyModernScrollBar(scrollPane);
        }

        // Process all components in the container
        Component[] components = container.getComponents();
        for (Component component : components) {
            // Update JLabel text color
            if (component instanceof JLabel label) {
                label.setForeground(textColor);

                // Update label icon color if needed
                if (label.getIcon() instanceof ImageIcon icon) {
                    if (shouldColorIcon(icon)) {
                        changeIconColor(icon, textColor);
                        label.repaint();
                    }
                }
            }
            // Update buttons
            else if (component instanceof JButton button) {
                if (!button.getText().isEmpty()) {
                    styleButton(button, backgroundColor, textColor, accentColor);
                }

                // Handle button icons
                if (button.getIcon() instanceof ImageIcon icon) {
                    if (shouldColorIcon(icon)) {
                        changeButtonIconColor(button);
                    }
                    button.repaint();
                }
            }
            // Update text components
            else if (component instanceof JTextComponent textComponent) {
                textComponent.setForeground(textColor);
                textComponent.setCaretColor(textColor);
                if (!(textComponent instanceof JTextArea)) {
                    textComponent.setBackground(darkenColor(backgroundColor, 0.1f));
                }
            } else if (component instanceof JSlider slider) {
                slider.setBackground(GuiUtil.lightenColor(backgroundColor, 0.1f));
                slider.setForeground(accentColor);
            } else if (component instanceof JList<?> list) {
                list.setBackground(backgroundColor);
                list.setForeground(textColor);
                list.setSelectionBackground(accentColor);
                list.setSelectionForeground(
                        calculateContrast(accentColor, textColor) > 4.5 ? textColor : backgroundColor);
            }
            // Update custom panels that have specific update methods
            else if (component instanceof ExpandableCardPanel expandableCard) {
                expandableCard.updateColors(backgroundColor, textColor);
            }

            // Recursively update child containers
            if (component instanceof Container childContainer) {
                updatePanelColors(childContainer, backgroundColor, textColor, accentColor);
            }
        }
    }

    public static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);
        menuBar.setBorderPainted(false);
        menuBar.setBackground(ThemeManager.getInstance().getBackgroundColor());
        menuBar.setForeground(ThemeManager.getInstance().getTextColor());
        return menuBar;
    }

    public static JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOpaque(true);
        toolBar.setBorderPainted(false);
        toolBar.setBackground(ThemeManager.getInstance().getBackgroundColor());
        toolBar.setForeground(ThemeManager.getInstance().getTextColor());
        return toolBar;
    }

    private static boolean shouldColorIcon(ImageIcon icon) {
        // Get the icon as a BufferedImage for analysis
        BufferedImage img = iconToBufferedImage(icon);
        if (img == null) return false;

        // Calculate characteristics that help determine if it's a simple icon or a complex image

        // 1. Check transparency - most simple icons have high transparency areas
        int transparentPixels = 0;
        int totalPixels = img.getWidth() * img.getHeight();
        int colorVariety = 0;
        java.util.Set<Integer> uniqueColors = new java.util.HashSet<>();

        // Sample pixels to keep this efficient
        int sampleRate = Math.max(1, img.getWidth() * img.getHeight() / 1000);

        for (int y = 0; y < img.getHeight(); y += sampleRate) {
            for (int x = 0; x < img.getWidth(); x += sampleRate) {
                int rgb = img.getRGB(x, y);
                int alpha = (rgb >>> 24) & 0xFF;

                if (alpha < 128) {
                    transparentPixels++;
                } else {
                    uniqueColors.add(rgb & 0x00FFFFFF);
                }
            }
        }

        double transparencyRatio = (double) transparentPixels / ((double) totalPixels / sampleRate);
        colorVariety = uniqueColors.size();


        boolean isSmallSize = img.getWidth() <= 48 && img.getHeight() <= 48;
        boolean hasHighTransparency = transparencyRatio > 0.4;
        boolean hasLowColorVariety = colorVariety < 20;

        if (icon.getDescription() != null) {
            String path = icon.getDescription().toLowerCase();
            if (path.contains("icon") || path.contains("svg") ||
                    path.contains("button") || path.contains("symbol")) {
                return true;
            }
        }

        return (isSmallSize && (hasHighTransparency || hasLowColorVariety));
    }


    private static BufferedImage iconToBufferedImage(ImageIcon icon) {
        if (icon == null) return null;

        // If the icon already has a BufferedImage, use it
        if (icon.getImage() instanceof BufferedImage) {
            return (BufferedImage) icon.getImage();
        }

        // Otherwise create a new BufferedImage from the icon
        BufferedImage img = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = img.createGraphics();
        icon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();

        return img;
    }

    public static JScrollPane createStyledScrollPane(Component contentPanel) {
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(ThemeManager.getInstance().getBackgroundColor());
        applyModernScrollBar(scrollPane);
        return scrollPane;
    }

    public static JLabel createErrorLabel(String content) {
        JLabel errorLabel = createLabel(content);
        errorLabel.setFont(FontUtil.getSpotifyFont(Font.ITALIC, 12));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        return errorLabel;
    }

    public static JLabel createUserAvatar(UserDTO user, int size, Color backgroundColor, Color textColor) {
        BufferedImage avatarImage = null;
        boolean useDefaultAvatar = false;

        try {
            if (user.getAvatar() != null) {
                BufferedImage originalImage = ImageIO.read(new File(user.getAvatar().getFileUrl()));
                avatarImage = createSmoothCircularAvatar(originalImage, size);
            } else {
                useDefaultAvatar = true;
            }
        } catch (IOException e) {
            useDefaultAvatar = true;
        }

        if (useDefaultAvatar) {
            avatarImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = avatarImage.createGraphics();

            configureGraphicsForHighQuality(g2d);

            g2d.setColor(backgroundColor);
            g2d.fillOval(0, 0, size, size);

            String initial = user.getUsername() != null && !user.getUsername().isEmpty() ?
                    user.getUsername().substring(0, 1).toUpperCase() : "U";

            float fontSize = (float) size * 0.4f;
            g2d.setColor(textColor);
            g2d.setFont(FontUtil.getSpotifyFont(Font.BOLD, fontSize));

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(initial);
            int textHeight = fm.getAscent();

            int x = (size - textWidth) / 2;
            int y = (size - textHeight) / 2 + fm.getAscent();

            g2d.drawString(initial, x, y);
            g2d.dispose();
        }

        return new JLabel(new ImageIcon(avatarImage));
    }

    public static JLabel createArtistAvatar(ArtistDTO artist, int size) {

        BufferedImage avatarImage;

        try {
            // Try to load the artist's profile picture
            if (artist.getProfilePicture() != null) {
                BufferedImage originalImage = ImageIO.read(new File(artist.getProfilePicture()));
                avatarImage = createSmoothCircularAvatar(originalImage, size);
            } else {
                // Create default avatar with initial
                avatarImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = avatarImage.createGraphics();

                configureGraphicsForHighQuality(g2d);

                // Fill background circle
                g2d.setColor(ThemeManager.getInstance().getBackgroundColor());
                g2d.fillOval(0, 0, size, size);

                // Draw initial
                String initial = artist.getStageName() != null && !artist.getStageName().isEmpty() ?
                        artist.getStageName().substring(0, 1).toUpperCase() :
                        "A";

                g2d.setColor(ThemeManager.getInstance().getTextColor());
                g2d.setFont(FontUtil.getSpotifyFont(Font.BOLD, (float) size / 2));

                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(initial);
                int textHeight = fm.getAscent();
                int x = (size - textWidth) / 2;
                int y = (size - textHeight) / 2 + textHeight;

                g2d.drawString(initial, x, y);
                g2d.dispose();
            }
        } catch (Exception e) {
            avatarImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = avatarImage.createGraphics();
            configureGraphicsForHighQuality(g2d);
            g2d.setColor(ThemeManager.getInstance().getBackgroundColor());
            g2d.fillOval(0, 0, size, size);
            g2d.dispose();
        }

        return new JLabel(new ImageIcon(avatarImage));
    }


    public static void addHoverEffect(JPanel panel) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setOpaque(true);
                panel.setBackground(GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f));
                panel.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setOpaque(false);
                panel.repaint();
            }
        });
    }

    public static JLabel createInteractiveUserAvatar(UserDTO user, int size, Color backgroundColor,
                                                     Color textColor, JMenuItem... popupItems) {
        JLabel avatarLabel = createUserAvatar(user, size, backgroundColor, textColor);
        avatarLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Ensure the label size matches the image size
        avatarLabel.setPreferredSize(new Dimension(size, size));

        // Add popup menu if items are provided
        if (popupItems.length > 0) {
            JPopupMenu popupMenu = createPopupMenu(backgroundColor, textColor);
            for (JMenuItem item : popupItems) {
                popupMenu.add(item);
            }

            avatarLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            });
        }

        return avatarLabel;
    }

    public static void showNetworkErrorDialog(Component parentComponent, String message) {
        Color errorColor = new Color(231, 76, 60);
        showCustomMessageDialog(parentComponent,
                "Network Error: " + message + "\nPlease check your internet connection and try again."
                , "Error", JOptionPane.ERROR_MESSAGE,
                errorColor);
    }

    public static void showErrorMessageDialog(Component parentComponent, String message) {
        Color errorColor = new Color(231, 76, 60); // A more refined red
        showCustomMessageDialog(parentComponent, message, "Error", JOptionPane.ERROR_MESSAGE
                , errorColor);
    }

    public static void showSuccessMessageDialog(Component parentComponent, String message) {
        Color successColor = new Color(46, 204, 113); // Emerald green
        showCustomMessageDialog(parentComponent, message, "Success", JOptionPane.PLAIN_MESSAGE,
                successColor);
    }

    public static void showWarningMessageDialog(Component parentComponent, String message) {
        Color warningColor = new Color(241, 196, 15); // Vibrant yellow
        showCustomMessageDialog(parentComponent, message, "Warning", JOptionPane.WARNING_MESSAGE,
                warningColor);
    }

    public static void showInfoMessageDialog(Component parentComponent, String message) {
        Color infoColor = new Color(52, 152, 219); // Light blue
        showCustomMessageDialog(parentComponent, message, "Information", JOptionPane.INFORMATION_MESSAGE,
                infoColor);
    }

    public static int showCustomConfirmDialog(Component parent, String message, String title,
                                              Color bgColor, Color textColor, Color accentColor) {
        final int[] result = new int[1];
        result[0] = JOptionPane.CLOSED_OPTION;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));

        setGradientBackground(mainPanel, bgColor, darkenColor(bgColor, 0.2f), 0.5f, 0.5f, 0.5f);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        styleDialog(dialog, darkenColor(bgColor, 0.1f), textColor);

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setFont(FontUtil.getSpotifyFont(Font.PLAIN, 14));
        messageLabel.setForeground(textColor);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create custom icon
        Icon icon = createCustomDialogIcon(JOptionPane.QUESTION_MESSAGE, 50, accentColor.darker(), bgColor);
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

        JPanel buttonPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);
        dialog.setVisible(true);

        return result[0];
    }

    public static int showConfirmMessageDialog(Component parentComponent, String message, String title) {
        return showCustomConfirmDialog(parentComponent, message, title,
                ThemeManager.getInstance().getBackgroundColor(), ThemeManager.getInstance().getTextColor(), new Color(52, 152, 219));
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
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
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


    public static JDialog createCustomMessageDialog(Component parent, String message, String title, int messageType, Color color) {
        Color bgColor = ThemeManager.getInstance().getBackgroundColor();
        Color textColor = ThemeManager.getInstance().getTextColor();
        // Create a custom JDialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);

        // Create main panel with gradient background
        JPanel mainPanel = GuiUtil.createPanel(new BorderLayout(20, 20));
        setGradientBackground(mainPanel, bgColor, darkenColor(bgColor, 0.2f), 0.5f, 0.5f, 0.5f);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add title bar styling
        styleDialog(dialog, darkenColor(bgColor, 0.1f), textColor);

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

        Icon icon = createColoredIcon(iconPath, 40, color);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setVerticalAlignment(SwingConstants.TOP);

        // Add icon and message to panel
        mainPanel.add(iconLabel, BorderLayout.WEST);
        mainPanel.add(messageLabel, BorderLayout.CENTER);

        // Create and style OK button
        JButton okButton = new JButton("OK");
        okButton.setBackground(color);
        okButton.setForeground(textColor);
        okButton.setBorderPainted(false);
        okButton.setContentAreaFilled(true);
        okButton.setFont(FontUtil.getSpotifyFont(Font.BOLD, 14));
        okButton.addActionListener(e -> dialog.dispose());

        // Add hover effect
        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                okButton.setBackground(darkenColor(color, 0.2f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                okButton.setBackground(color);
            }
        });

        // Create button panel
        JPanel buttonPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(okButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set dialog content
        dialog.setContentPane(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        return dialog;
    }

    public static void showCustomMessageDialog(Component parent, String message, String title, int messageType, Color accentColor) {
        JDialog dialog = createCustomMessageDialog(parent, message, title, messageType, accentColor);
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

    public static Icon createColoredIcon(String iconPath, int size) {
        try {
            BufferedImage original = ImageIO.read(new File(iconPath));
            if (original == null) throw new IOException("Image is null");

            BufferedImage resized = Thumbnails.of(original).size(size, size).asBufferedImage();
            ImageIcon icon = new ImageIcon(resized);
            changeIconColor(icon, ThemeManager.getInstance().getTextColor());
            return icon;
        } catch (Exception e) {
            e.printStackTrace();
            return createPlaceholderIcon(size);
        }
    }

    public static Icon createColoredIcon(String iconPath, int size, Color color) {
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


    public static Color[] extractThemeColors(BufferedImage image) {
        if (image == null) {
            // Return default colors if no image is provided
            return new Color[]{
                    AppConstant.BACKGROUND_COLOR,
                    AppConstant.TEXT_COLOR,
                    GuiUtil.darkenColor(AppConstant.BACKGROUND_COLOR, 0.1f)
            };
        }

        try {
            // Extract dominant colors using ColorThief
            int[][] dominantColors = ColorThief.getPalette(image, 5, 1, false);

            if (dominantColors != null && dominantColors.length >= 3) {
                Color primaryColor = new Color(dominantColors[0][0], dominantColors[0][1], dominantColors[0][2]);
                Color secondaryColor = new Color(dominantColors[1][0], dominantColors[1][1], dominantColors[1][2]);
                Color extractedAccentColor = new Color(dominantColors[2][0], dominantColors[2][1], dominantColors[2][2]);

                // Enhance primary color for better visual appeal
                primaryColor = enhanceColor(primaryColor);

                // Use primary color as background
                Color extractedBgColor = primaryColor;

                // Determine text color based on contrast ratio with background
                Color extractedTextColor;
                double contrastRatio = calculateContrast(primaryColor, secondaryColor);

                if (contrastRatio < 4.5) { // W3C AA standard for contrast
                    extractedTextColor = createHighContrastTextColor(primaryColor);
                } else {
                    extractedTextColor = secondaryColor;
                }

                // Ensure accent color has good contrast with background
                if (calculateContrast(extractedBgColor, extractedAccentColor) < 3.0) {
                    // If accent doesn't have enough contrast, adjust it
                    extractedAccentColor = enhanceColor(extractedAccentColor);

                    // If still not enough contrast, create a more vibrant accent
                    if (calculateContrast(extractedBgColor, extractedAccentColor) < 3.0) {
                        float[] hsb = Color.RGBtoHSB(
                                extractedAccentColor.getRed(),
                                extractedAccentColor.getGreen(),
                                extractedAccentColor.getBlue(),
                                null);

                        // Increase saturation and adjust brightness for better visibility
                        hsb[1] = Math.min(1.0f, hsb[1] * 1.5f);  // Increase saturation
                        hsb[2] = Math.min(Math.max(0.6f, hsb[2]), 0.9f);  // Ensure good brightness

                        extractedAccentColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
                    }
                }

                return new Color[]{extractedBgColor, extractedTextColor, extractedAccentColor};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Return default colors if extraction fails
        return new Color[]{
                AppConstant.BACKGROUND_COLOR,
                AppConstant.TEXT_COLOR,
                GuiUtil.darkenColor(AppConstant.BACKGROUND_COLOR, 0.1f)
        };
    }

    private static Color enhanceColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        // Slightly increase saturation for more vibrant colors
        hsb[1] = Math.min(1.0f, hsb[1] * 1.1f);

        // Adjust brightness to ensure it's not too dark or too light
        if (hsb[2] < 0.2f) {
            hsb[2] = 0.2f; // Ensure dark colors are visible
        } else if (hsb[2] > 0.9f) {
            hsb[2] = 0.9f; // Prevent colors that are too bright
        }

        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }


    private static Color createHighContrastTextColor(Color backgroundColor) {
        float[] hsb = Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), null);

        // For darker backgrounds, use lighter text
        if (hsb[2] < 0.5) {
            return new Color(245, 245, 245); // Almost white
        } else {
            // For lighter backgrounds, use darker text
            return new Color(25, 25, 25); // Almost black
        }
    }

}
