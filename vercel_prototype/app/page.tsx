import { PackingList } from "@/components/packing-list"

export default function Page() {
  return (
    <main className="flex min-h-screen items-start justify-center bg-[oklch(0.92_0.003_260)] py-4 sm:py-8">
      {/* Mobile device frame */}
      <div className="relative w-full max-w-[430px] min-h-[calc(100vh-2rem)] sm:min-h-[860px] overflow-hidden bg-background sm:rounded-[2.5rem] sm:md3-elevation-2 sm:border sm:border-border/50">
        <PackingList />
      </div>
    </main>
  )
}
