import csv
import os
from datetime import datetime
import random

# 创建目标目录（如果不存在）
output_dir = "/app/prediction_result"
os.makedirs(output_dir, exist_ok=True)

# 生成文件名（带时间戳）

filename = f"result.csv"
filepath = os.path.join(output_dir, filename)

# 示例数据 - 可以根据实际需求修改
headers = ["id", "model_name", "prediction", "confidence", "timestamp"]
data = [
    [1001, "resnet50", "cat", 0.92, datetime.now().isoformat()],
    [1002, "resnet50", "dog", 0.87, datetime.now().isoformat()],
    [1003, "inception_v3", "bird", 0.78, datetime.now().isoformat()],
    [1004, "inception_v3", "car", 0.95, datetime.now().isoformat()],
    [1005, "yolov5", "person", 0.89, datetime.now().isoformat()]
]

# 生成并保存CSV文件
with open(filepath, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    
    # 写入表头
    writer.writerow(headers)
    
    # 写入数据行
    writer.writerows(data)

print(f"CSV文件已成功生成: {filepath}")
print(f"文件包含 {len(data)} 条预测记录")