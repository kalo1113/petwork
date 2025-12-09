import os
import shutil
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing import image

# --- 配置 ---
MODEL_PATH = 'models/model_2.h5'
# 混合图片所在的文件夹
INPUT_FOLDER = 'dataset/test'
# 图片尺寸（必须与训练时一致）
IMAGE_SIZE = (100, 100)

# --- 加载模型 ---
print(f"正在加载模型: {MODEL_PATH}...")
model = load_model(MODEL_PATH)
print("模型加载完毕。")

# --- 准备输出文件夹 ---
cat_output_folder = os.path.join(INPUT_FOLDER, 'cat')
dog_output_folder = os.path.join(INPUT_FOLDER, 'dog')

os.makedirs(cat_output_folder, exist_ok=True)
os.makedirs(dog_output_folder, exist_ok=True)

# --- 分类并移动文件 ---
image_files = [f for f in os.listdir(INPUT_FOLDER) if f.lower().endswith(('.png', '.jpg', '.jpeg', '.gif'))]

if not image_files:
    print(f"在 '{INPUT_FOLDER}' 文件夹中没有找到任何图片。")
else:
    print(f"找到了 {len(image_files)} 张图片，开始分类...")
    for filename in image_files:
        # 避免处理已经分类过的图片
        if filename in ['cat', 'dog']:
            continue

        img_path = os.path.join(INPUT_FOLDER, filename)

        try:
            # 1. 加载并预处理图片
            img = image.load_img(img_path, target_size=IMAGE_SIZE)
            img_array = image.img_to_array(img) / 255.0
            img_array = np.expand_dims(img_array, axis=0)

            # 2. 使用模型进行预测
            prediction = model.predict(img_array)[0][0]

            # 3. 根据预测结果决定目标文件夹
            if prediction < 0.5:
                predicted_class = 'cat'
                confidence = (1 - prediction) * 100
                target_folder = cat_output_folder
            else:
                predicted_class = 'dog'
                confidence = prediction * 100
                target_folder = dog_output_folder

            # 4. 移动图片
            destination_path = os.path.join(target_folder, filename)
            shutil.move(img_path, destination_path)

            print(f"'{filename}' -> 预测为: {predicted_class} (置信度: {confidence:.2f}%)")

        except Exception as e:
            print(f"处理文件 '{filename}' 时出错: {e}")

    print("\n分类完成！请查看 'mixed_test' 文件夹下的 'cat' 和 'dog' 子文件夹。")