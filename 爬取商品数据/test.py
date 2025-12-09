import requests

# 手动登录后获取的Cookies
cookies = [
    {"name": "cookie1", "value": "VypWH1A6oMT%2FBjcAORXfO1MzbxrRxj461Mte4ZGIqSE%3D"},
    {"name": "cookie2", "value": "10f59275d1af36f6535f74c5a74397bb"}
]

# 创建一个会话
session = requests.Session()
# 添加Cookies到会话
for cookie in cookies:
    session.cookies.set(cookie["name"], cookie["value"])

# 使用会话发送请求
response = session.get('https://www.taobao.com/')
print(response.text)