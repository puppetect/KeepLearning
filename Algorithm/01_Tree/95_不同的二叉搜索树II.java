/*给定一个整数 n，生成所有由 1 ... n 为节点所组成的 二叉搜索树 。*/

/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
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

