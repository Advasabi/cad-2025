import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AutoserviceAdmin {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame loginFrame = new JFrame("Вход в систему автосервиса");
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setSize(400, 250);
            loginFrame.setLocationRelativeTo(null);

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);

            usernameField.setText("moderator");
            passwordField.setText("123");

            gbc.gridx = 0; gbc.gridy = 0;
            panel.add(new JLabel("Логин:"), gbc);
            gbc.gridx = 1;
            panel.add(usernameField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            panel.add(new JLabel("Пароль:"), gbc);
            gbc.gridx = 1;
            panel.add(passwordField, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            JButton loginButton = new JButton("Войти");
            panel.add(loginButton, gbc);

            loginFrame.add(panel);
            loginFrame.setVisible(true);

            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (testConnection(username, password)) {
                    loginFrame.dispose();
                    showMainWindow(username, password);
                } else {
                    JOptionPane.showMessageDialog(loginFrame,
                            "Неверный логин/пароль или сервер недоступен",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }

    private static boolean testConnection(String username, String password) {
        try {
            String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            URL url = new URL("http://localhost:8080/api/moderator/orders");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", authHeader);
            conn.setConnectTimeout(5000);
            int code = conn.getResponseCode();
            conn.disconnect();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static void showMainWindow(String username, String password) {
        JFrame mainFrame = new JFrame("Панель модератора - Автосервис");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1300, 750);
        mainFrame.setLocationRelativeTo(null);

        ModeratorPanel moderatorPanel = new ModeratorPanel(username, password);
        mainFrame.add(moderatorPanel);
        mainFrame.setVisible(true);
    }
}

class ModeratorPanel extends JPanel {
    private String authHeader;
    private JTable ordersTable;
    private DefaultTableModel ordersModel;
    private JLabel statusLabel;

    public ModeratorPanel(String username, String password) {
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        setupUI();
        loadOrders();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Панель статуса
        statusLabel = new JLabel("Готово");
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);

        // Таблица заказов
        String[] columns = {"ID", "Клиент", "Марка", "Номер", "Описание", "Статус", "Дата создания"};
        ordersModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        ordersTable = new JTable(ordersModel);
        ordersTable.setRowHeight(25);
        ordersTable.setAutoCreateRowSorter(true);

        // Цветовая индикация статусов
        ordersTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getValueAt(row, 5); // столбец "Статус"
                if ("NEW".equals(status)) {
                    c.setBackground(new Color(255, 243, 205)); // светло-желтый
                } else if ("IN_PROGRESS".equals(status)) {
                    c.setBackground(new Color(209, 236, 241)); // светло-голубой
                } else if ("COMPLETED".equals(status)) {
                    c.setBackground(new Color(212, 237, 218)); // светло-зеленый
                } else if ("CANCELLED".equals(status)) {
                    c.setBackground(new Color(248, 215, 218)); // светло-красный
                } else {
                    c.setBackground(Color.WHITE);
                }
                if (isSelected) {
                    c.setBackground(new Color(200, 200, 255)); // цвет выделения
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton refreshBtn = new JButton("Обновить");
        JButton changeStatusBtn = new JButton("Изменить статус");
        JButton deleteBtn = new JButton("Удалить");

        changeStatusBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        buttonPanel.add(refreshBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(changeStatusBtn);
        buttonPanel.add(deleteBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // Обработчики
        refreshBtn.addActionListener(e -> loadOrders());
        changeStatusBtn.addActionListener(e -> changeOrderStatus());
        deleteBtn.addActionListener(e -> deleteOrder());

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = ordersTable.getSelectedRow() >= 0;
                changeStatusBtn.setEnabled(hasSelection);
                deleteBtn.setEnabled(hasSelection);
            }
        });
    }

    private void loadOrders() {
        statusLabel.setText("Загрузка заказов...");
        new Thread(() -> {
            try {
                String json = fetchData("http://localhost:8080/api/moderator/orders");
                SwingUtilities.invokeLater(() -> {
                    updateOrdersTable(json);
                    statusLabel.setText("Заказы загружены");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Ошибка загрузки заказов");
                    JOptionPane.showMessageDialog(this,
                            "Ошибка загрузки заказов: " + e.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private String fetchData(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", authHeader);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("HTTP ошибка: " + code);
        }

        BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    private void updateOrdersTable(String json) {
        ordersModel.setRowCount(0);
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return;
        }

        try {
            // Удаляем обрамляющие скобки массива
            json = json.trim();
            if (json.startsWith("[") && json.endsWith("]")) {
                json = json.substring(1, json.length() - 1);
            }

            String[] objects = json.contains("},{") ? json.split("\\},\\{") : new String[]{json};

            for (int i = 0; i < objects.length; i++) {
                String obj = objects[i];
                if (i == 0 && obj.startsWith("{")) obj = obj.substring(1);
                if (i == objects.length - 1 && obj.endsWith("}")) obj = obj.substring(0, obj.length() - 1);

                String id = getJsonValue(obj, "id");
                String clientFullName = getNestedJsonValue(obj, "client", "fullName"); // получаем ФИО клиента из вложенного объекта
                String carBrand = getJsonValue(obj, "carBrand");
                String carNumber = getJsonValue(obj, "carNumber");
                String description = getJsonValue(obj, "description");
                String status = getJsonValue(obj, "status");
                String createdAt = getJsonValue(obj, "createdAt");
                if (createdAt.length() > 10) createdAt = createdAt.substring(0, 10); // только дата

                ordersModel.addRow(new Object[]{id, clientFullName, carBrand, carNumber, description, status, createdAt});
            }

        } catch (Exception e) {
            statusLabel.setText("Ошибка парсинга: " + e.getMessage());
        }
    }

    // Получение простого значения из JSON
    private String getJsonValue(String json, String key) {
        try {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search);
            if (start == -1) return "";

            start += search.length();

            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }

            if (start >= json.length()) return "";

            char firstChar = json.charAt(start);
            if (firstChar == '"') {
                start++;
                int end = json.indexOf('"', start);
                return end == -1 ? "" : json.substring(start, end);
            } else {
                int end = start;
                while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
                    end++;
                }
                return json.substring(start, end).trim();
            }
        } catch (Exception e) {
            return "";
        }
    }

    // Получение значения из вложенного объекта, например client.fullName
    private String getNestedJsonValue(String json, String outerKey, String innerKey) {
        String outer = getJsonValue(json, outerKey);
        if (outer.isEmpty() || !outer.startsWith("{")) return "";
        return getJsonValue(outer, innerKey);
    }

    private void changeOrderStatus() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) return;

        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) ordersModel.getValueAt(modelRow, 0);
        String currentStatus = (String) ordersModel.getValueAt(modelRow, 5);

        // Диалог выбора нового статуса
        String[] statuses = {"NEW", "IN_PROGRESS", "COMPLETED", "CANCELLED"};
        String newStatus = (String) JOptionPane.showInputDialog(this,
                "Выберите новый статус для заказа #" + id,
                "Изменение статуса",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                currentStatus);

        if (newStatus == null || newStatus.equals(currentStatus)) return;

        statusLabel.setText("Изменение статуса...");

        final String orderId = id;
        final String selectedStatus = newStatus;

        new Thread(() -> {
            try {
                updateOrderStatus(orderId, selectedStatus);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Статус заказа #" + orderId + " изменён на " + selectedStatus,
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadOrders(); // обновить таблицу
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка: " + e.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Ошибка изменения статуса");
                });
            }
        }).start();
    }

    private void updateOrderStatus(String orderId, String newStatus) throws Exception {
        String urlStr = "http://localhost:8080/api/moderator/orders/" + orderId + "/status?status=" + URLEncoder.encode(newStatus, "UTF-8");

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Authorization", authHeader);
        conn.setConnectTimeout(10000);

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("HTTP ошибка: " + code);
        }
        conn.disconnect();
    }

    private void deleteOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) return;

        int modelRow = ordersTable.convertRowIndexToModel(row);
        String id = (String) ordersModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите удалить заказ #" + id + "?",
                "Подтверждение удаления",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        statusLabel.setText("Удаление заказа...");

        final String orderId = id;

        new Thread(() -> {
            try {
                sendDeleteRequest(orderId);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Заказ #" + orderId + " удалён",
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadOrders();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка: " + e.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Ошибка удаления");
                });
            }
        }).start();
    }

    private void sendDeleteRequest(String orderId) throws Exception {
        String urlStr = "http://localhost:8080/api/moderator/orders/" + orderId;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", authHeader);
        conn.setConnectTimeout(10000);

        int code = conn.getResponseCode();
        if (code != 200 && code != 204) { // 204 No Content тоже успех
            throw new Exception("HTTP ошибка: " + code);
        }
        conn.disconnect();
    }
}