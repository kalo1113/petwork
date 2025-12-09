import os
import numpy as np
import pandas as pd
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score

# 1. 配置路径（根据你的实际路径修改）
TEST_DIR = "dataset/test"  # 测试集根目录（包含cat和dog子文件夹）
MODEL_DIR = "models"  # 模型保存的文件夹
IMAGE_SIZE = (100, 100)  # 和训练时的图片尺寸保持一致
BATCH_SIZE = 32

# 2. 加载测试集
test_datagen = ImageDataGenerator(rescale=1. / 255)  # 只做归一化，不增强
test_generator = test_datagen.flow_from_directory(
    TEST_DIR,
    target_size=IMAGE_SIZE,
    batch_size=BATCH_SIZE,
    class_mode='binary',  # 二分类（猫=0，狗=1）
    shuffle=False  # 不打乱顺序，确保预测结果和真实标签对应
)
y_true = test_generator.classes  # 真实标签

# 3. 评估所有模型并记录结果
results = []
model_files = [f for f in os.listdir(MODEL_DIR) if f.endswith('.h5')]

for model_file in model_files:
    model_path = os.path.join(MODEL_DIR, model_file)
    print(f"\n正在评估模型：{model_file}")

    # 加载模型
    model = load_model(model_path)

    # 预测
    y_pred_prob = model.predict(test_generator)  # 预测概率
    y_pred = (y_pred_prob > 0.5).astype(int).flatten()  # 转为0/1标签

    # 计算指标
    metrics = {
        "模型名称": model_file,
        "准确率": accuracy_score(y_true, y_pred),
        "精确率": precision_score(y_true, y_pred),
        "召回率": recall_score(y_true, y_pred),
        "F1分数": f1_score(y_true, y_pred)
    }
    results.append(metrics)
    print(f"准确率: {metrics['准确率']:.4f}")
    print(f"F1分数: {metrics['F1分数']:.4f}")

# 4. 生成对比表格并保存
df = pd.DataFrame(results)
df = df.sort_values(by="F1分数", ascending=False)  # 按F1分数降序排列
df.to_csv("模型评估结果.csv", index=False, encoding="utf-8-sig")
print("\n评估完成！结果已保存为：模型评估结果.csv")
print("\n最优模型排序：")
print(df[["模型名称", "F1分数", "准确率"]])