import os
import shutil
import random
import argparse
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Conv2D, Flatten, MaxPooling2D, Dropout
from tensorflow.keras.optimizers import Adam
from sklearn.model_selection import train_test_split

# --- 命令行参数解析 ---
parser = argparse.ArgumentParser()
parser.add_argument('--epochs', type=int, default=20, help='训练轮次')
parser.add_argument('--lr', type=float, default=0.0001, help='学习率')
args = parser.parse_args()

# --- 1. 配置参数 ---
IMG_WIDTH, IMG_HEIGHT = 100, 100
BATCH_SIZE = 32
EPOCHS = args.epochs
LEARNING_RATE = args.lr
VALIDATION_SPLIT = 0.2

# 从环境变量获取路径。原始数据目录被挂载为只读。
ORIGINAL_TRAIN_DATA_DIR = os.getenv('TRAIN_DATA_DIR', '/app/dataset/train')
MODEL_SAVE_PATH = os.getenv('MODEL_SAVE_PATH', '/app/models/cat_dog_classifier.h5')

# 在容器内部创建一个临时工作目录，用于存放数据集副本
WORKING_DIR = '/tmp/training_workdir'
TEMP_TRAIN_DATA_DIR = os.path.join(WORKING_DIR, 'train')
TEMP_VALID_DATA_DIR = os.path.join(WORKING_DIR, 'validation')

# --- 2. 核心修复：复制数据集到容器内部的临时目录 ---
print("Preparing dataset...")

# 确保工作目录干净
if os.path.exists(WORKING_DIR):
    shutil.rmtree(WORKING_DIR)
os.makedirs(WORKING_DIR, exist_ok=True)

# 复制原始训练数据到临时目录
print(f"Copying dataset from {ORIGINAL_TRAIN_DATA_DIR} to {TEMP_TRAIN_DATA_DIR}...")
shutil.copytree(ORIGINAL_TRAIN_DATA_DIR, TEMP_TRAIN_DATA_DIR)
print("Dataset copy complete.")

# --- 3. 在临时目录上进行数据划分和整理 ---
# 遍历所有图片，按文件名开头判断类别（0=猫，1=狗）
image_paths = []
labels = []
for filename in os.listdir(TEMP_TRAIN_DATA_DIR):
    prefix = filename.split('.')[0]
    if prefix not in ['0', '1']:
        print(f"Skipping unknown file: {filename} (文件名需以0或1开头)")
        continue

    label = 0 if prefix == '0' else 1
    src_path = os.path.join(TEMP_TRAIN_DATA_DIR, filename)
    image_paths.append(src_path)
    labels.append(label)

# 分割训练集和验证集
train_paths, val_paths, train_labels, val_labels = train_test_split(
    image_paths, labels, test_size=VALIDATION_SPLIT, random_state=42, stratify=labels
)

# 创建验证集目录的子文件夹
os.makedirs(os.path.join(TEMP_VALID_DATA_DIR, 'cat'), exist_ok=True)
os.makedirs(os.path.join(TEMP_VALID_DATA_DIR, 'dog'), exist_ok=True)

# 将验证集图片移动到对应目录
print("Moving validation images...")
for path, label in zip(val_paths, val_labels):
    filename = os.path.basename(path)
    dest_subdir = 'cat' if label == 0 else 'dog'
    dest_path = os.path.join(TEMP_VALID_DATA_DIR, dest_subdir, filename)
    shutil.move(path, dest_path)

# 整理训练集目录（按类别存放）
os.makedirs(os.path.join(TEMP_TRAIN_DATA_DIR, 'cat'), exist_ok=True)
os.makedirs(os.path.join(TEMP_TRAIN_DATA_DIR, 'dog'), exist_ok=True)

print("Organizing training images...")
for path, label in zip(train_paths, train_labels):
    filename = os.path.basename(path)
    dest_subdir = 'cat' if label == 0 else 'dog'
    dest_path = os.path.join(TEMP_TRAIN_DATA_DIR, dest_subdir, filename)
    shutil.move(path, dest_path)

# --- 4. 使用ImageDataGenerator加载数据 ---
print("Loading data...")

train_datagen = ImageDataGenerator(
    rescale=1. / 255,
    shear_range=0.2,
    zoom_range=0.2,
    horizontal_flip=True
)

val_datagen = ImageDataGenerator(rescale=1. / 255)

train_generator = train_datagen.flow_from_directory(
    TEMP_TRAIN_DATA_DIR,
    target_size=(IMG_WIDTH, IMG_HEIGHT),
    batch_size=BATCH_SIZE,
    class_mode='binary'
)

validation_generator = val_datagen.flow_from_directory(
    TEMP_VALID_DATA_DIR,
    target_size=(IMG_WIDTH, IMG_HEIGHT),
    batch_size=BATCH_SIZE,
    class_mode='binary'
)

# --- 5. 构建并训练模型 ---
model = Sequential([
    Conv2D(32, (3, 3), activation='relu', input_shape=(IMG_WIDTH, IMG_HEIGHT, 3)),
    MaxPooling2D(pool_size=(2, 2)),
    Conv2D(64, (3, 3), activation='relu'),
    MaxPooling2D(pool_size=(2, 2)),
    Conv2D(128, (3, 3), activation='relu'),
    MaxPooling2D(pool_size=(2, 2)),
    Conv2D(128, (3, 3), activation='relu'),
    MaxPooling2D(pool_size=(2, 2)),
    Flatten(),
    Dense(512, activation='relu'),
    Dropout(0.5),
    Dense(1, activation='sigmoid')
])

model.compile(
    loss='binary_crossentropy',
    optimizer=Adam(learning_rate=LEARNING_RATE),
    metrics=['accuracy']
)

print("Starting training...")
history = model.fit(
    train_generator,
    steps_per_epoch=train_generator.samples // BATCH_SIZE,
    epochs=EPOCHS,
    validation_data=validation_generator,
    validation_steps=validation_generator.samples // BATCH_SIZE
)

# --- 6. 保存模型 ---
os.makedirs(os.path.dirname(MODEL_SAVE_PATH), exist_ok=True)
model.save(MODEL_SAVE_PATH)
print(f"Model saved to {MODEL_SAVE_PATH}")

# 训练结束后，可以选择清理临时文件（可选）
# print("Cleaning up temporary files...")
# shutil.rmtree(WORKING_DIR)