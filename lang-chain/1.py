"""
LangChain 远程大模型调用 Demo（OpenAI 兼容接口）

安装依赖：
    pip install -U langchain langchain-openai

运行前先设置环境变量：
    export LLM_BASE_URL="https://openrouter.ai/api/v1"
    export LLM_API_KEY="your_openrouter_key"
    export LLM_MODEL="openrouter/auto"

执行：
    python 1.py
"""

import os

from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI
from openai import APIError


def build_llm() -> ChatOpenAI:
    base_url = os.getenv("LLM_BASE_URL", "https://openrouter.ai/api/v1")
    api_key = os.getenv("LLM_API_KEY", "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")
    model = os.getenv("LLM_MODEL", "openrouter/auto")

    if not base_url:
        raise ValueError("请先设置环境变量 LLM_BASE_URL")
    if not api_key:
        raise ValueError("请先设置环境变量 LLM_API_KEY")

    return ChatOpenAI(
        model=model,
        base_url=base_url,
        api_key=api_key,
        temperature=0.7,
    )


def basic_chat_demo(llm: ChatOpenAI) -> None:
    response = llm.invoke("请用一句话介绍 LangChain 的核心作用。")
    print("\n=== 基础调用 ===")
    print(response.content)


def prompt_chain_demo(llm: ChatOpenAI) -> None:
    prompt = ChatPromptTemplate.from_messages(
        [
            ("system", "你是一个简洁、专业的技术助手。"),
            ("human", "请用3条要点解释 {topic}，每条不超过20字。"),
        ]
    )
    chain = prompt | llm
    response = chain.invoke({"topic": "RAG"})

    print("\n=== Prompt + Chain 调用 ===")
    print(response.content)


def main() -> None:
    llm = build_llm()
    try:
        basic_chat_demo(llm)
        prompt_chain_demo(llm)
    except APIError as e:
        status_code = getattr(e, "status_code", None)
        print(f"\n调用失败: {e}")
        if status_code == 401:
            print("提示: API Key 无效或未配置，请检查 LLM_API_KEY。")
        elif status_code == 403:
            print("提示: 当前模型在你所在区域不可用，换一个模型再试。")
        elif status_code == 402:
            print("提示: 当前账号额度不足或模型需付费，请改用基础/免费模型。")
        elif status_code == 404:
            print("提示: 模型名或接口路径不正确，请检查 LLM_MODEL / LLM_BASE_URL。")
        raise


if __name__ == "__main__":
    main()