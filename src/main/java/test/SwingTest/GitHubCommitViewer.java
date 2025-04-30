package test.SwingTest;

import org.kohsuke.github.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GitHubCommitViewer extends JFrame {
    private JTextField repoField;
    private JButton fetchButton;
    private JTable commitTable;
    private DefaultTableModel tableModel;

    public GitHubCommitViewer() {
        setTitle("GitHub Commit Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel nhập repository
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Repository (owner/repo):"));
        repoField = new JTextField(20);
        inputPanel.add(repoField);
        fetchButton = new JButton("Fetch Commits");
        inputPanel.add(fetchButton);
        add(inputPanel, BorderLayout.NORTH);

        // Bảng hiển thị commit
        String[] columns = {"SHA", "Author", "Date", "Message"};
        tableModel = new DefaultTableModel(columns, 0);
        commitTable = new JTable(tableModel);
        add(new JScrollPane(commitTable), BorderLayout.CENTER);

        // Sự kiện nút Fetch
        fetchButton.addActionListener(e -> fetchCommits());
    }

    private void fetchCommits() {
        String repoInput = repoField.getText().trim();
        if (repoInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập repository (owner/repo)");
            return;
        }

        // Xóa dữ liệu bảng cũ
        tableModel.setRowCount(0);

        try {
            // Kết nối GitHub API
            GitHub github = new GitHubBuilder().withOAuthToken("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").build();
            GHRepository repo = github.getRepository(repoInput);

            // Lấy danh sách branch
            List<GHBranch> branches = repo.getBranches().values().stream().toList();

            // Lưu trữ commit không trùng lặp
            Set<String> seenCommits = new HashSet<>();
            List<GHCommit> allCommits = new ArrayList<>();

            // Lấy commit từ mỗi branch
            for (GHBranch branch : branches) {
                PagedIterable<GHCommit> commits = repo.queryCommits().from(branch.getName()).list();
                for (GHCommit commit : commits) {
                    String sha = commit.getSHA1();
                    if (!seenCommits.contains(sha)) {
                        seenCommits.add(sha);
                        allCommits.add(commit);
                    }
                }
            }

            // Thêm commit vào bảng
            for (GHCommit commit : allCommits) {
                String author = commit.getAuthor() != null ? commit.getAuthor().getLogin() : "Unknown";
                String date = commit.getCommitDate().toString();
                String message = commit.getCommitShortInfo().getMessage();
                tableModel.addRow(new Object[]{commit.getSHA1(), author, date, message});
            }

            JOptionPane.showMessageDialog(this, "Đã lấy " + allCommits.size() + " commit.");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GitHubCommitViewer().setVisible(true);
        });
    }
}
