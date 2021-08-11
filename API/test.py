import requests
import json

post_url='http://open.edukg.cn/opedukg/api/typeAuth/user/login'
search_url='http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList'
detail_url='http://open.edukg.cn/opedukg/api/typeOpen/open/infoByInstanceName'
question_url='http://open.edukg.cn/opedukg/api/typeOpen/open/inputQuestion'
ner_url='http://open.edukg.cn/opedukg/api/typeOpen/open/linkInstance'
ps_url='http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName'
link_url='http://open.edukg.cn/opedukg/api/typeOpen/open/relatedsubject'

def main():  

    headers={
        'Content-Type': 'application/json;charset=UTF-8',
    }
    
    # 用之前把自己账号密码填进去

    payload={
        'password':'',
        'phone':''
    }

    r=requests.post(post_url,data=json.dumps(payload), headers=headers)
    print(r.status_code)
    print(r.text)
    res_data=json.loads(r.text)
    login_id=res_data['id']
    print(login_id)

    # detail(login_id)
    # ner(login_id)
    # problem_set(login_id)
    # know_link(login_id)


# 差最后一个接口：知识链接属性详情接口 没写 没看懂干啥的...

def know_link(login_id,course='chinese',subjectName='李白'):
    link_header={
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',        
    }
    link_params={
        'course':course,
        'subjectName':subjectName,
        'id':login_id
    }
    r=requests.post(link_url,headers=link_header,data=link_params)
    print(r.status_code)
    # print(r.text)
    jsonData=json.loads(r.text)
    with open(link_params['subjectName']+'.json','w',encoding='utf-8') as f:
        f.write(json.dumps(jsonData,ensure_ascii=False))
def problem_set(login_id,uriName='http://edukb.org/knowledge/0.1/instance/english#-1a9c6c8704459e99a5564ecb0e6c9faf'):
    ps_header={
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',        
    }
    ps_params={
        'uriName':uriName,
        'id':login_id
    }
    r=requests.get(ps_url,headers=ps_header,params=ps_params)
    print(r.status_code)
    print(r.text)
    jsonData=json.loads(r.text)
    with open(ps_params['uriName'].split('/')[-1]+'.json','w',encoding='utf-8') as f:
        f.write(json.dumps(jsonData,ensure_ascii=False))
def ner(login_id,course='english',context='welcome to the hotel california'):
    ner_header={
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',        
    }
    ner_params={
        'course':course,
        'context':context,
        'id':login_id
    }
    # r=requests.post(question_url,params=ques_params,headers=ques_header,data=ques_params)
    r=requests.post(ner_url,headers=ner_header,data=ner_params)
    print(r.status_code)
    print(r.text)
    jsonData=json.loads(r.text)
    with open(ner_params['context']+'.json','w',encoding='utf-8') as f:
        f.write(json.dumps(jsonData,ensure_ascii=False))
def question(login_id,course='english',inputQuestion='how are you'):
    ques_header={
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',        
    }
    ques_params={
        'course':course,
        'inputQuestion':inputQuestion,
        'id':login_id
    }
    # r=requests.post(question_url,params=ques_params,headers=ques_header,data=ques_params)
    r=requests.post(question_url,headers=ques_header,data=ques_params)
    print(r.status_code)
    print(r.text)
    jsonData=json.loads(r.text)
    with open(ques_params['inputQuestion']+'.json','w',encoding='utf-8') as f:
        f.write(json.dumps(jsonData,ensure_ascii=False))

def detail(login_id):
    detail_header={
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
    }
    detail_params={
        'course':'chinese',
        'name':'苹果之争',
        'id':login_id
    }
    r=requests.get(detail_url,headers=detail_header,params=detail_params)
    print(r.status_code)
    print(r.text)
    jsonData=json.loads(r.text)
    with open(detail_params['name']+'.json','w',encoding='utf-8') as f:
        f.write(json.dumps(jsonData,ensure_ascii=False))

def search(login_id):
    search_header={
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
    }
    search_params={
        'course':'chinese',
        'searchKey':'苹果',
        'id':login_id
    }
    r=requests.get(search_url,headers=search_header,params=search_params)
    print(r.status_code)
    print(r.text)
    search_res=json.loads(r.text)
    search_res_url=search_res['data'][0]['uri']
    print(search_res_url)
    # search_text=requests.get(search_res_url).text
    # print(search_text)

if __name__ == '__main__':
    main()