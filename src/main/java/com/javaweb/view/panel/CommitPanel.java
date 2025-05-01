package com.javaweb.view.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedIterable;

import com.javaweb.constant.AppConstant;
import com.javaweb.utils.DateUtil;
import com.javaweb.utils.GuiUtil;
import com.javaweb.utils.StringUtils;
import com.javaweb.view.theme.ThemeChangeListener;
import com.javaweb.view.theme.ThemeManager;

import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

@Slf4j
public class CommitPanel extends JPanel implements ThemeChangeListener {
    private JPanel commitsContent;
    private final String repoName = "VoxNut/MuseMoe";
    private JComboBox<String> branchComboBox;
    private List<String> branchNames = new ArrayList<>();

    public CommitPanel() {
        initialize();
        ThemeManager.getInstance().addThemeChangeListener(this);
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Header panel with branch filter and refresh button
        JPanel headerPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel branchLabel = GuiUtil.createLabel("Branch:", Font.BOLD, 14);
        branchComboBox = GuiUtil.createComboBox();
        branchComboBox.setPreferredSize(new Dimension(250, 28));
        branchComboBox.addItem("Loading branches...");
        branchComboBox.setEnabled(false);

        JButton refreshButton = GuiUtil.createIconButtonWithText("Refresh", AppConstant.REFRESH_ICON_PATH);

        headerPanel.add(branchLabel);
        headerPanel.add(branchComboBox);
        headerPanel.add(refreshButton);

        // Content panel for commits
        commitsContent = GuiUtil.createPanel();
        commitsContent.setLayout(new BoxLayout(commitsContent, BoxLayout.Y_AXIS));

        // Initial "loading" placeholder
        JLabel loadingLabel = GuiUtil.createLabel("Loading commits...", Font.ITALIC, 14);
        commitsContent.add(loadingLabel, "growx");

        JScrollPane scrollPane = GuiUtil.createStyledScrollPane(commitsContent);

        // Add components using BorderLayout
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Load branches first
        SwingUtilities.invokeLater(this::fetchBranches);

        // Add action listeners
        refreshButton.addActionListener(e -> fetchCommits(getSelectedBranch()));
        branchComboBox.addActionListener(e -> {
            if (branchComboBox.isEnabled() && branchComboBox.getSelectedItem() != null) {
                fetchCommits(getSelectedBranch());
            }
        });
    }

    private String getSelectedBranch() {
        Object selected = branchComboBox.getSelectedItem();
        if (selected == null || "All Branches".equals(selected)) {
            return null;
        }
        return selected.toString();
    }

    private void fetchBranches() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                try {
                    String token = System.getenv("GITHUB_TOKEN");
                    log.info("Using GitHub token (length: {})", token != null ? token.length() : 0);

                    GitHub github = new GitHubBuilder().withOAuthToken(token).build();
                    GHRepository repo = github.getRepository(repoName);
                    List<String> branches = new ArrayList<>();
                    branches.add("All Branches");

                    for (GHBranch branch : repo.getBranches().values()) {
                        branches.add(branch.getName());
                    }
                    return branches;
                } catch (IOException ex) {
                    log.error("Failed to fetch branches: {}", ex.getMessage(), ex);
                    throw ex;
                }
            }

            @Override
            protected void done() {
                try {
                    branchNames = get();
                    branchComboBox.removeAllItems();

                    for (String branch : branchNames) {
                        branchComboBox.addItem(branch);
                    }

                    branchComboBox.setEnabled(true);
                    // Fetch all commits initially
                    fetchCommits(null);
                } catch (Exception e) {
                    branchComboBox.removeAllItems();
                    branchComboBox.addItem("Error loading branches");
                    log.error("Failed to load branches: {}", e.getMessage(), e);
                }
            }
        }.execute();
    }

    private void fetchCommits(String branchName) {
        commitsContent.removeAll();

        JLabel loadingLabel = GuiUtil.createLabel("Fetching commits...", Font.ITALIC, 12);
        commitsContent.add(loadingLabel);
        commitsContent.revalidate();
        commitsContent.repaint();

        new SwingWorker<List<CommitInfo>, Void>() {
            @Override
            protected List<CommitInfo> doInBackground() throws Exception {
                try {
                    String token = System.getenv("GITHUB_TOKEN");
                    log.info("Using GitHub token (length: {})", token != null ? token.length() : 0);

                    GitHub github = new GitHubBuilder().withOAuthToken(token).build();
                    GHRepository repo = github.getRepository(repoName);

                    List<GHBranch> branches;
                    if (branchName == null) {
                        branches = repo.getBranches().values().stream().toList();
                    } else {
                        // Fetch only the selected branch
                        GHBranch branch = repo.getBranch(branchName);
                        branches = List.of(branch);
                    }

                    // Store unique commits
                    Set<String> seenCommits = new HashSet<>();
                    List<CommitInfo> allCommits = new ArrayList<>();

                    // Get commits from each branch
                    for (GHBranch branch : branches) {
                        PagedIterable<GHCommit> branchCommits = repo.queryCommits().from(branch.getName()).list();
                        for (GHCommit commit : branchCommits) {
                            String sha = commit.getSHA1();
                            if (!seenCommits.contains(sha)) {
                                seenCommits.add(sha);

                                // Create commit info object
                                String author = commit.getAuthor() != null ? commit.getAuthor().getLogin() : "Unknown";
                                String date = commit.getCommitDate().toString();
                                String message = commit.getCommitShortInfo().getMessage();

                                allCommits.add(new CommitInfo(sha, author, date, message, branch.getName()));
                            }
                        }
                    }

                    // Sort commits by date (newest first)
                    allCommits.sort((c1, c2) -> c2.date.compareTo(c1.date));
                    return allCommits;

                } catch (IOException ex) {
                    log.error("Failed to fetch commits: {}", ex.getMessage(), ex);
                    throw ex;
                }
            }

            @Override
            protected void done() {
                try {
                    List<CommitInfo> commits = get();
                    updateCommitDisplay(commits);
                } catch (Exception e) {
                    commitsContent.removeAll();
                    JLabel errorLabel = GuiUtil.createErrorLabel("Failed to fetch commits: " + e.getMessage());
                    commitsContent.add(errorLabel);
                    commitsContent.revalidate();
                    commitsContent.repaint();
                }
            }
        }.execute();
    }

    private void updateCommitDisplay(List<CommitInfo> commits) {
        commitsContent.removeAll();

        if (commits.isEmpty()) {
            JLabel emptyLabel = GuiUtil.createLabel("No commits found", Font.ITALIC, 12);
            commitsContent.add(emptyLabel);
            commitsContent.revalidate();
            commitsContent.repaint();
            return;
        }

        // Add a header
        JPanel headerPanel = GuiUtil.createPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel countLabel = GuiUtil.createLabel(
                "Showing " + commits.size() + " commits",
                Font.BOLD, 12);
        headerPanel.add(countLabel, BorderLayout.WEST);
        commitsContent.add(headerPanel);

        // Add each commit
        for (CommitInfo commit : commits) {
            JPanel commitPanel = createCommitItemPanel(commit);
            commitsContent.add(commitPanel);
            commitsContent.add(Box.createVerticalStrut(5));
        }

        commitsContent.revalidate();
        commitsContent.repaint();
    }

    private JPanel createCommitItemPanel(CommitInfo commit) {
        JPanel itemPanel = GuiUtil.createPanel(new MigLayout("fillx, insets 3 5 3 5", "[100]5[grow, center]5[150]", "[]"));
        itemPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                GuiUtil.darkenColor(ThemeManager.getInstance().getBackgroundColor(), 0.1f)));

        // Create hash and branch labels
        String shortSha = commit.sha.substring(0, 7);
        JLabel shaLabel = GuiUtil.createLabel(shortSha, Font.BOLD, 14);

        JLabel branchLabel = GuiUtil.createLabel("[" + commit.branch + "]", Font.ITALIC, 13);

        JPanel leftPanel = GuiUtil.createPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(shaLabel);
        leftPanel.add(branchLabel);

        // Create message label with center alignment
        JLabel messageLabel = GuiUtil.createLabel(StringUtils.getTruncatedText(commit.message, 100), Font.PLAIN, 14);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Format the date for Vietnam timezone
        String formattedDate = DateUtil.formatIsoDateToVietnamTime(commit.date);

        // Create author and date labels
        JLabel authorLabel = GuiUtil.createLabel(commit.author, Font.ITALIC, 13);
        JLabel dateLabel = GuiUtil.createLabel(formattedDate, Font.PLAIN, 12);
        dateLabel.setForeground(GuiUtil.darkenColor(ThemeManager.getInstance().getTextColor(), 0.3f));

        // Create info panel for author and date
        JPanel infoPanel = GuiUtil.createPanel(new MigLayout("", "[]5[]", "[]"));
        infoPanel.add(authorLabel);
        infoPanel.add(dateLabel);

        // Add all components to the item panel
        itemPanel.add(leftPanel, "cell 0 0");
        itemPanel.add(messageLabel, "cell 1 0, growx, center");
        itemPanel.add(infoPanel, "cell 2 0, right");

        // Add hover effect
        GuiUtil.addHoverEffect(itemPanel);

        return itemPanel;
    }

    @Override
    public void onThemeChanged(Color backgroundColor, Color textColor, Color accentColor) {
        GuiUtil.updatePanelColors(this, backgroundColor, textColor, accentColor);
    }

    private record CommitInfo(String sha, String author, String date, String message, String branch) {
    }
}