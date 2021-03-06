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
        inorderTraversalByRecursive(root);
        inorderTraversalByStack(root);
    }

    public List<Integer> inorderTraversalByRecursive(TreeNode root) {
        if(root== null) return res;
        inorderTraversal(root.left);
        res.put(root.val);
        inorderTraversal(root.right);
        return res;
    }


    public List<Integer> inorderTraversalByStack(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        Stack<TreeNode> stack = new Stack<>();
        TreeNode node = root;
        while(node != null || !stack.isEmpty()){
            if(node != null){
                stack.push(node);
                node = node.left;
            } else {
                node = stack.pop();
                res.add(node.val);
                node = node.right;
            }
        }
        return res;
    }


}

