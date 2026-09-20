<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { CheckCircle2 } from 'lucide-vue-next'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import Tabs from '@/app/components/common/misc/Tabs.vue'
import SettingsAside from '@/app/components/partial/settings/SettingsAside.vue'
import SettingsPreferencesPanel from '@/app/components/partial/settings/SettingsPreferencesPanel.vue'
import SettingsProfileCard from '@/app/components/partial/settings/SettingsProfileCard.vue'
import SettingsSecurityPanel from '@/app/components/partial/settings/SettingsSecurityPanel.vue'
import { useAuthStore } from '@/features/auth/stores/auth-store'
import { usersApi } from '../api/users-api'

const router = useRouter()
const auth = useAuthStore()
type SettingsTab = 'account' | 'notifications' | 'privacy' | 'location' | 'appearance' | 'language'
const activeTab = ref<SettingsTab>('account')
const fullName = ref('')
const firstName = ref('')
const lastName = ref('')
const phone = ref('')
const saving = ref(false)
const loading = ref(true)
const error = ref('')
const feedback = ref('')
const notificationsEnabled = ref(true)
const tabs = [
  { id: 'account' as const, label: 'Conta' },
  { id: 'notifications' as const, label: 'Notificações' },
  { id: 'privacy' as const, label: 'Privacidade' },
  { id: 'location' as const, label: 'Localização' },
  { id: 'appearance' as const, label: 'Aparência' },
  { id: 'language' as const, label: 'Idioma' },
]

function hydrate() {
  fullName.value = [auth.user?.firstName, auth.user?.lastName].filter(Boolean).join(' ')
  phone.value = auth.user?.phone ?? ''
}

function updateFullName(value: string) {
  const parts = value.trim().split(/\s+/).filter(Boolean)
  fullName.value = value
  if (parts.length > 1) {
    firstName.value = parts.shift() ?? ''
    lastName.value = parts.join(' ')
  } else {
    firstName.value = parts[0] ?? ''
    lastName.value = ''
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    await auth.initialize()
    hydrate()
  } catch {
    error.value = 'Não foi possível carregar as configurações da conta.'
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  saving.value = true
  feedback.value = ''
  error.value = ''
  try {
    const updated = await usersApi.updateMe({ firstName: firstName.value.trim(), lastName: lastName.value.trim(), phone: phone.value.trim() || null })
    auth.user = updated
    hydrate()
    feedback.value = 'As informações do perfil foram atualizadas.'
  } catch {
    error.value = 'Não foi possível guardar as alterações. Tente novamente.'
  } finally {
    saving.value = false
  }
}

function showComingSoon() {
  feedback.value = 'Esta preferência estará disponível em breve.'
}

function changePassword() {
  void router.push({ name: 'forgot-password', query: { email: auth.user?.email } })
}

function deleteAccount() {
  feedback.value = 'A eliminação de conta estará disponível em breve. Contacte o suporte para continuar.'
}

onMounted(() => { void load() })
</script>

<template>
  <div class="mx-auto max-w-287.5 container px-7 pb-10 max-[600px]:px-4">
    <header class="pt-3.25 pb-4"><h1 class="m-0 text-[28px] leading-[1.15] font-semibold tracking-[-1px] text-deep-navy">Configurações</h1><p class="mt-1 mb-0 text-sm text-linkops-slate-500">Personalize a sua conta, preferências e notificações.</p></header>
    <Tabs v-model="activeTab" :tabs="tabs" />
    <p v-if="feedback" class="mt-3 rounded-md border border-green-100 bg-green-50 px-3 py-2 text-xs text-green-800" role="status"><CheckCircle2 class="mr-1 inline size-4 align-text-bottom" />{{ feedback }}</p>
    <div v-if="loading" class="mt-4 grid grid-cols-[minmax(0,1fr)_262px] gap-5 max-[950px]:grid-cols-1"><div class="grid gap-3"><div v-for="item in 4" :key="item" class="h-24 animate-pulse rounded-lg border border-slate-200 bg-slate-50"></div></div><div class="h-80 animate-pulse rounded-lg border border-slate-200 bg-slate-50"></div></div>
    <div v-else class="mt-3 grid grid-cols-[minmax(0,1fr)_262px] items-start gap-5 max-[950px]:grid-cols-1">
      <main class="grid min-w-0 gap-3.5">
        <div v-if="error" class="rounded-lg border border-red-100 bg-red-50 p-4 text-sm text-red-700" role="alert">{{ error }}</div>
        <template v-if="activeTab === 'account'">
          <SettingsProfileCard :full-name="fullName" :phone="phone" :email="auth.user?.email ?? ''" :saving="saving" @update:full-name="updateFullName" @update:phone="phone = $event" @save="saveProfile" />
          <SettingsSecurityPanel @password="changePassword" @help="showComingSoon" />
          <SettingsPreferencesPanel v-model:notifications-enabled="notificationsEnabled" @help="showComingSoon" />
        </template>
        <section v-else class="rounded-lg border border-linkops-slate-200 bg-white p-6" aria-live="polite"><h2 class="m-0 text-base font-semibold text-deep-navy">{{ tabs.find(tab => tab.id === activeTab)?.label }}</h2><p class="mt-2 mb-4 text-sm text-linkops-slate-500">Estas opções estarão disponíveis em breve.</p><AppButton variant="secondary" size="sm" @click="showComingSoon">Guardar preferência</AppButton></section>
      </main>
      <SettingsAside :user="auth.user" @delete="deleteAccount" />
    </div>
  </div>
</template>
