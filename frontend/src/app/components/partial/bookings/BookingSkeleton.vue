<template>
  <div class="flex flex-col gap-[9px]" aria-busy="true" aria-label="A carregar pedidos">
    <div v-for="item in 3" :key="item" class="grid min-h-[126px] grid-cols-[124px_minmax(0,1fr)] items-center gap-[16px] rounded-[9px] border border-[#E9EDF0] px-[12px] py-[10px] max-[720px]:grid-cols-[88px_minmax(0,1fr)] max-[720px]:gap-[11px] max-[720px]:px-[9px] max-[720px]:py-[9px]">
      <span class="h-[88px] w-[124px] animate-pulse rounded-[7px] bg-[#F0F3F4] max-[720px]:h-[74px] max-[720px]:w-[88px]"></span>
      <div class="flex flex-col gap-[10px]"><span class="h-[14px] w-[40%] animate-pulse rounded-[5px] bg-[#F0F3F4]"></span><span class="h-[11px] w-[68%] animate-pulse rounded-[5px] bg-[#F0F3F4]"></span><span class="h-[11px] w-[30%] animate-pulse rounded-[5px] bg-[#F0F3F4]"></span></div>
    </div>
  </div>
</template>
