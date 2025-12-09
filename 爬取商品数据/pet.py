import random
import time
import os
import requests
import pymysql
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# 数据库配置（根据你的Navicat修改）
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'lxt1113',  # 你的MySQL密码
    'db': 'petwork',  # 数据库名
    'port': 3306,
    'charset': 'utf8mb4'
}


# 初始化图片保存目录（修改为目标路径）
def init_image_dir():
    # 图片保存根目录：F:\毕设\新建文件夹\pet-backend\productimg
    img_root = r'F:\毕设\新建文件夹\pet-backend\productimg'
    if not os.path.exists(img_root):
        os.makedirs(img_root)  # 自动创建目录（包括多级目录）
    return img_root


# 下载图片（按“大类_序号.jpg”命名）
def download_image(img_url, main_category, index, img_dir):
    try:
        # 处理大类名称中的特殊字符（避免创建目录失败）
        main_cat = main_category.replace('/', '-').replace('\\', '-').replace(':', '-')
        # 创建大类子目录（如：productimg\狗狗主粮）
        cat_dir = os.path.join(img_dir, main_cat)
        if not os.path.exists(cat_dir):
            os.makedirs(cat_dir)
        # 图片文件名：大类_序号.jpg
        img_name = f"{main_cat}_{index}.jpg"
        img_path = os.path.join(cat_dir, img_name)

        # 下载图片（添加请求头模拟浏览器，避免被拦截）
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36'
        }
        response = requests.get(img_url, headers=headers, timeout=10)
        with open(img_path, 'wb') as f:
            f.write(response.content)
        print(f"图片保存：{img_path}")
        return img_path
    except Exception as e:
        print(f"图片下载失败：{e}")
        return None


# 连接数据库
def get_db_connection():
    return pymysql.connect(**DB_CONFIG)


# 保存商品到数据库（保留description字段，适配数据库结构）
def save_to_db(data):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        sql = """
        INSERT INTO product (main_category, title, `description`, now_price, old_price, img_path)
        VALUES (%s, %s, %s, %s, %s, %s)
        """
        cursor.execute(sql, (
            data['main_category'],
            data['title'],
            data['description'],  # 保留description字段，避免数据库报错
            data['now_price'],
            data['old_price'],
            data['img_path']
        ))
        conn.commit()
        print("商品已保存到数据库")
    except Exception as e:
        print(f"数据库保存失败：{e}")
        conn.rollback()
    finally:
        if conn:
            conn.close()


def crawl_category(category_name, link_class):
    """只按大类爬取，不含小分类逻辑"""
    img_dir = init_image_dir()  # 初始化图片保存目录
    try:
        # 点击目标分类（狗狗站/猫咪站）
        category_link = WebDriverWait(browser, 10).until(
            EC.element_to_be_clickable((By.CSS_SELECTOR, f"a.it1.{link_class}"))
        )
        category_link.click()
        print(f"\n已点击【{category_name}】分类")
        time.sleep(random.uniform(2, 4))

        # 定位所有大类容器（如“狗狗主粮”“狗狗零食”）
        all_category_boxes = WebDriverWait(browser, 15).until(
            EC.presence_of_all_elements_located((By.CSS_SELECTOR, "div.goods-menu-box.w-max.ct"))
        )
        print(f"共找到 {len(all_category_boxes)} 个大类区域")

        # 遍历每个大类
        for box in all_category_boxes:
            try:
                # 提取大类标题（唯一分类维度）
                main_category = box.find_element(By.CSS_SELECTOR, "div.c333.ft22").text.strip()
                print(f"\n=== 开始爬取【{main_category}】大类 ===")

                # 定位当前大类的商品容器（默认显示的商品）
                goods_container = WebDriverWait(box, 10).until(
                    EC.presence_of_element_located(
                        (By.CSS_SELECTOR, "div[id^='cateInfo_'][style*='display: block']")
                    )
                )

                # 提取商品项
                goods_items = goods_container.find_elements(By.CSS_SELECTOR, "div.goods-one.bgfff")
                print(f"找到 {len(goods_items)} 个商品项")

                # 遍历商品并保存
                for index, item in enumerate(goods_items, 1):
                    try:
                        # 提取商品信息（保留description字段）
                        title = item.find_element(By.CSS_SELECTOR, "div.goods-title").text.strip()
                        description = item.find_element(By.CSS_SELECTOR, "div.c999.ft12").text.strip()
                        now_price = item.find_element(By.CSS_SELECTOR, "span.price-now").text.strip()
                        old_price = item.find_element(By.CSS_SELECTOR, "span.price-old").text.strip()
                        img_url = item.find_element(By.CSS_SELECTOR, "img").get_attribute("src")

                        # 下载图片
                        img_path = download_image(img_url, main_category, index, img_dir)
                        if not img_path:
                            continue  # 图片下载失败则跳过当前商品

                        # 保存到数据库
                        product_data = {
                            'main_category': main_category,
                            'title': title,
                            'description': description,
                            'now_price': now_price,
                            'old_price': old_price,
                            'img_path': img_path
                        }
                        save_to_db(product_data)

                        # 打印信息
                        print(f"\n商品 {index}:")
                        print(f"标题: {title}")
                        print(f"描述: {description[:30]}...")  # 只显示前30个字符
                        print(f"价格: {now_price} (原价: {old_price})")

                    except Exception as e:
                        print(f"商品 {index} 处理失败: {str(e)[:50]}...")

            except Exception as e:
                print(f"大类【{main_category}】解析失败: {e}")

        # 返回首页
        browser.get('https://www.epet.com/')
        time.sleep(random.uniform(2, 3))

    except Exception as e:
        print(f"【{category_name}】点击失败: {e}")


try:
    # 初始化浏览器
    browser = webdriver.Chrome()
    browser.maximize_window()
    browser.implicitly_wait(15)

    # 访问首页
    browser.get('https://www.epet.com/')
    print("已打开E宠商城首页")
    time.sleep(random.uniform(2, 3))

    # 爬取分类（先测试狗狗站）
    # crawl_category("狗狗站", "dog-box")
    crawl_category("猫咪站", "cat-box")  # 测试通过后开启

except Exception as e:
    print(f"整体执行错误: {e}")

finally:
    # 关闭浏览器
    time.sleep(5)
    browser.quit()
    print("\n爬虫执行完毕，浏览器已关闭")