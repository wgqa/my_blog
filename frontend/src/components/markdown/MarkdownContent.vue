<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

const props = defineProps<{
  contentHtml: string
}>()

const contentRef = ref<HTMLElement>()

watch(
  () => props.contentHtml,
  async () => {
    await nextTick()
    highlightCode()
    await renderMermaid()
  },
  { immediate: false },
)

onMounted(async () => {
  await nextTick()
  highlightCode()
  await renderMermaid()
})

const highlightCode = () => {
  contentRef.value?.querySelectorAll('pre').forEach((pre) => {
    const block = pre.querySelector('code:not(.language-mermaid)')
    if (!block) return

    hljs.highlightElement(block as HTMLElement)

    if (pre.parentElement?.classList.contains('code-block-wrapper')) return

    const lang = extractLanguage(block)
    const wrapper = document.createElement('div')
    wrapper.className = 'code-block-wrapper'

    const toolbar = document.createElement('div')
    toolbar.className = 'code-block-toolbar'
    toolbar.innerHTML = `<span class="code-block-lang">${lang}</span><button class="code-block-copy" title="复制代码">复制</button>`

    toolbar.querySelector('.code-block-copy')?.addEventListener('click', async () => {
      const text = (block as HTMLElement).textContent || ''
      try {
        await navigator.clipboard.writeText(text)
        toolbar.querySelector('.code-block-copy')!.textContent = '已复制'
        setTimeout(() => {
          toolbar.querySelector('.code-block-copy')!.textContent = '复制'
        }, 2000)
      } catch {
        // fallback
      }
    })

    pre.parentNode?.insertBefore(wrapper, pre)
    wrapper.appendChild(toolbar)
    wrapper.appendChild(pre)
  })
}

const extractLanguage = (block: Element): string => {
  for (const cls of block.classList) {
    if (cls.startsWith('language-')) {
      return cls.slice(9)
    }
  }
  return 'code'
}

const renderMermaid = async () => {
  const blocks = contentRef.value?.querySelectorAll('code.language-mermaid')
  if (!blocks?.length) return

  const { default: mermaid } = await import('mermaid')
  mermaid.initialize({ startOnLoad: false })

  for (const block of blocks) {
    const pre = block.parentElement
    if (!pre || pre.tagName !== 'PRE') continue
    const id = `mermaid-${Math.random().toString(36).slice(2, 9)}`
    try {
      const { svg } = await mermaid.render(id, block.textContent || '')
      pre.outerHTML = svg
    } catch {
      // fallback: leave raw code block
    }
  }
}
</script>

<template>
  <div ref="contentRef" class="markdown-content" v-html="contentHtml" />
</template>
