<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { categoriesApi } from '@/features/categories/api/categories-api'
import type { Category } from '@/features/categories/types/category'
import { servicesApi } from '@/features/services/api/services-api'
import type { ServiceOffering, ServiceQuery } from '@/features/services/types/service'
import { getErrorMessage } from '@/shared/api/api-error'
import type { PageMetadata } from '@/shared/types/api'

const filters = reactive<ServiceQuery>({ q: '', category: '', city: '', page: 0, size: 12 })
const services = ref<ServiceOffering[]>([])
const categories = ref<Category[]>([])
const page = ref<PageMetadata>({ size: 12, number: 0, totalElements: 0, totalPages: 0 })
const loading = ref(false)
const error = ref('')

const categoryOptions = computed(() => categories.value.flatMap((parent) => [
  { slug: parent.slug, name: parent.name },
  ...parent.children.map((child) => ({ slug: child.slug, name: `${parent.name} — ${child.name}` })),
]))

async function loadServices(targetPage = 0) {
  loading.value = true
  error.value = ''
  filters.page = targetPage

  try {
    const response = await servicesApi.list(filters)
    services.value = response.content
    page.value = response.page
  } catch (requestError) {
    error.value = getErrorMessage(requestError)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    categories.value = await categoriesApi.list()
  } catch (requestError) {
    error.value = getErrorMessage(requestError)
  }
  await loadServices()
})
</script>

<template>
  <section class="catalog-page">
    <header>
      <p class="eyebrow">Catálogo público</p>
      <h1>Encontrar serviços</h1>
      <p class="muted">Esta página é uma integração funcional de referência para a equipa de frontend.</p>
    </header>

    <form class="filters" @submit.prevent="loadServices(0)">
      <label>
        Pesquisa
        <input v-model.trim="filters.q" type="search" placeholder="Ex.: canalizador" />
      </label>
      <label>
        Categoria
        <select v-model="filters.category">
          <option value="">Todas</option>
          <option v-for="category in categoryOptions" :key="category.slug" :value="category.slug">
            {{ category.name }}
          </option>
        </select>
      </label>
      <label>
        Cidade
        <input v-model.trim="filters.city" placeholder="Ex.: Maputo" />
      </label>
      <button type="submit" :disabled="loading">Pesquisar</button>
    </form>

    <p v-if="error" class="form-error" role="alert">{{ error }}</p>
    <p v-else-if="loading" class="muted">A carregar serviços…</p>
    <p v-else-if="!services.length" class="empty-state">Nenhum serviço encontrado.</p>

    <div v-else class="card-grid">
      <article v-for="service in services" :key="service.id" class="service-card">
        <p class="eyebrow">{{ service.categoryName }}</p>
        <h2>{{ service.title }}</h2>
        <p>{{ service.description || 'Sem descrição.' }}</p>
        <p class="muted">{{ service.providerName }} · {{ service.city }}</p>
        <strong>{{ service.priceType === 'NEGOTIABLE' ? 'Preço negociável' : `${service.price} MZN` }}</strong>
      </article>
    </div>

    <nav v-if="page.totalPages > 1" class="pagination" aria-label="Paginação">
      <button type="button" :disabled="loading || page.number === 0" @click="loadServices(page.number - 1)">
        Anterior
      </button>
      <span>Página {{ page.number + 1 }} de {{ page.totalPages }}</span>
      <button type="button" :disabled="loading || page.number + 1 >= page.totalPages" @click="loadServices(page.number + 1)">
        Seguinte
      </button>
    </nav>
  </section>
</template>
