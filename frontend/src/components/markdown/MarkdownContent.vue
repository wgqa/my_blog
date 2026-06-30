<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue'

const props = defineProps<{
  contentHtml: string
}>()

const contentRef = ref<HTMLElement>()

watch(
  () => props.contentHtml,
  async () => {
    await nextTick()
    await renderMermaid()
  },
  { immediate: false },
)

onMounted(async () => {
  await nextTick()
  await renderMermaid()
})

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
