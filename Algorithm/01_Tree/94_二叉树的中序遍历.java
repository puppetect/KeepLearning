/*
给定一个二叉树，返回它的中序 遍历。
*/

/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {

    public static void main(String[] args) {
        List<Integer> res = new ArrayList<>();
        inorderTraversal(root)
    }
    public List<Integer> inorderTraversal(TreeNode root) {
        if(root== null) return res;
        inorderTraversal(root.left);
        res.put(root.val);
        inorderTraversal(root.right);
        return res;
    }
}
