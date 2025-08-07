import pandas as pd

# 读取原始CSV文件（假设文件名为train.csv）
df = pd.read_csv('总数据.csv')

# 提取从第201行开始的所有数据（索引200到末尾）
# 注意：pandas的iloc索引从0开始，第201行对应索引200
subset = df.iloc[200:]

# 提取最后一列'risk_flag'
ground_truth = subset[['risk_flag']]

# 保存到新的CSV文件（不包含索引）
ground_truth.to_csv('Ground_Truth.csv', index=False)