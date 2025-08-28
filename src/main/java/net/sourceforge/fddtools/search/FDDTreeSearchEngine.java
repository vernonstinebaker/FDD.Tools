package net.sourceforge.fddtools.search;

import net.sourceforge.fddtools.model.FDDINode;
import javafx.scene.control.TreeItem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fuzzy search engine for FDD tree nodes.
 * Provides fuzzy matching on node names with scoring and ranking.
 */
public class FDDTreeSearchEngine {
    
    /**
     * Represents a search match with the matched node and its score.
     */
    public static class SearchMatch {
        private final TreeItem<FDDINode> treeItem;
        private final FDDINode node;
        private final double score;
        private final String matchedText;
        
        public SearchMatch(TreeItem<FDDINode> treeItem, FDDINode node, double score, String matchedText) {
            this.treeItem = treeItem;
            this.node = node;
            this.score = score;
            this.matchedText = matchedText;
        }
        
        public TreeItem<FDDINode> getTreeItem() { return treeItem; }
        public FDDINode getNode() { return node; }
        public double getScore() { return score; }
        public String getMatchedText() { return matchedText; }
        
        @Override
        public String toString() {
            return String.format("SearchMatch{node=%s, score=%.2f}", 
                node != null ? node.getName() : "null", score);
        }
    }
    
    /**
     * Searches for nodes matching the query string.
     * @param rootItem the root tree item to search from
     * @param query the search query string
     * @return list of matches sorted by relevance score (highest first)
     */
    public List<SearchMatch> search(TreeItem<FDDINode> rootItem, String query) {
        if (query == null || query.trim().isEmpty() || rootItem == null) {
            return Collections.emptyList();
        }
        
        String normalizedQuery = query.trim().toLowerCase();
        List<SearchMatch> matches = new ArrayList<>();
        
        // Recursively search all nodes
        searchRecursive(rootItem, normalizedQuery, matches);
        
        // Sort by score (highest first) and return
        return matches.stream()
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }
    
    /**
     * Recursively searches through tree items.
     */
    private void searchRecursive(TreeItem<FDDINode> item, String query, List<SearchMatch> matches) {
        if (item == null) return;
        
        FDDINode node = item.getValue();
        if (node != null && node.getName() != null) {
            double score = calculateFuzzyScore(node.getName(), query);
            if (score > 0) {
                matches.add(new SearchMatch(item, node, score, node.getName()));
            }
        }
        
        // Search children
        for (TreeItem<FDDINode> child : item.getChildren()) {
            searchRecursive(child, query, matches);
        }
    }
    
    /**
     * Calculates fuzzy match score between node name and query.
     * Higher score = better match.
     * @param nodeName the name of the node to match against
     * @param query the search query
     * @return score between 0 (no match) and 1 (perfect match)
     */
    private double calculateFuzzyScore(String nodeName, String query) {
        if (nodeName == null || query == null) return 0;
        
        String normalizedName = nodeName.toLowerCase();
        String normalizedQuery = query.toLowerCase();
        
        // Exact match gets highest score
        if (normalizedName.equals(normalizedQuery)) {
            return 1.0;
        }
        
        // Starts with query gets high score
        if (normalizedName.startsWith(normalizedQuery)) {
            return 0.9;
        }
        
        // Contains query gets medium score
        if (normalizedName.contains(normalizedQuery)) {
            return 0.7;
        }
        
        // Fuzzy match using simple character sequence matching
        double fuzzyScore = calculateSequenceScore(normalizedName, normalizedQuery);
        
        // Only return if fuzzy score is reasonable (lowered threshold for better matching)
        return fuzzyScore > 0.2 ? fuzzyScore * 0.6 : 0;
    }
    
    /**
     * Calculates score based on character sequence matching.
     * This is a simplified fuzzy matching algorithm.
     */
    private double calculateSequenceScore(String text, String query) {
        if (text.length() == 0 || query.length() == 0) return 0;
        
        int matches = 0;
        int queryIndex = 0;
        
        // Count sequential character matches
        for (int i = 0; i < text.length() && queryIndex < query.length(); i++) {
            if (text.charAt(i) == query.charAt(queryIndex)) {
                matches++;
                queryIndex++;
            }
        }
        
        // Score based on how many query characters were matched
        double matchRatio = (double) matches / query.length();
        
        // Bonus for shorter text (more specific matches)
        double lengthPenalty = Math.min(1.0, (double) query.length() / text.length());
        
        return matchRatio * lengthPenalty;
    }
    
    /**
     * Gets the path from root to the given tree item for breadcrumb display.
     * @param item the tree item to get path for
     * @return list of node names from root to item
     */
    public List<String> getNodePath(TreeItem<FDDINode> item) {
        List<String> path = new ArrayList<>();
        TreeItem<FDDINode> current = item;
        
        while (current != null) {
            FDDINode node = current.getValue();
            if (node != null && node.getName() != null) {
                path.add(0, node.getName()); // Add to beginning
            }
            current = current.getParent();
        }
        
        return path;
    }
}
