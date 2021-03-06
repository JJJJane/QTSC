# QuadTreeDP
基于四分树的差分隐私二维数据划分算法
在查分隐私模型中，利用区间书结构进行二维空间数据发布是降低较大区域计数查询误差的一种有效方式。基本思想是将二维空间数据映射成一颗区间树，
树中节点对应二维空间数据的某个子区域，从而将二维区域计数查询表示为较少的若干区间树节点的噪声统计值之和，以达到降低查询结果噪声误差的目的。
均衡噪声误差和均匀假设误差是提高发布二维空间数据的区域计数查询误差的关键。
首先构造二维空间数据集D所对应的四分树，并往树中节点添加隐私预算e1=ae（a为0-1之间的差分隐私分配参数），得到差分隐私四分树；接着设计启发式策略，
合并差分隐私四分树的部分节点，而后对合并调整后的四分树中节点重新添加隐私预算为e2=（1-a）e的噪声，最后，采用GBLUE迭代算法对差分隐私四分树进行后置处理，
使得树中节点的噪声统计值满足查询一致性约束。
