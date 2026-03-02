"use client"

import { useState, useRef } from "react"
import { Plus, ChevronDown } from "lucide-react"
import { cn } from "@/lib/utils"

interface AddItemFormProps {
  onAdd: (name: string, category: string) => void
  categories: string[]
  placeholder?: string
}

export function AddItemForm({
  onAdd,
  categories,
  placeholder = "Item name...",
}: AddItemFormProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [name, setName] = useState("")
  const [category, setCategory] = useState(categories[0] || "General")
  const inputRef = useRef<HTMLInputElement>(null)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = name.trim()
    if (!trimmed) return
    onAdd(trimmed, category)
    setName("")
    inputRef.current?.focus()
  }

  if (!isOpen) {
    return (
      <button
        onClick={() => {
          setIsOpen(true)
          setTimeout(() => inputRef.current?.focus(), 50)
        }}
        className="flex w-full items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-border bg-card/50 py-3.5 text-sm font-medium text-muted-foreground transition-all hover:border-primary/30 hover:text-primary active:scale-[0.98]"
      >
        <Plus className="h-4 w-4" />
        Add item
      </button>
    )
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="overflow-hidden rounded-2xl bg-card md3-elevation-1"
    >
      <div className="flex flex-col gap-3 p-3">
        <input
          ref={inputRef}
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder={placeholder}
          className="w-full rounded-xl border-0 bg-muted/50 px-3.5 py-2.5 text-sm text-foreground placeholder:text-muted-foreground/50 focus:outline-none focus:ring-2 focus:ring-primary/30"
          autoFocus
        />
        <div className="flex items-center gap-2">
          {categories.length > 1 && (
            <div className="relative flex-1">
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className={cn(
                  "w-full appearance-none rounded-xl border-0 bg-muted/50 py-2.5 pl-3.5 pr-8 text-sm text-foreground",
                  "focus:outline-none focus:ring-2 focus:ring-primary/30"
                )}
              >
                {categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
              <ChevronDown className="pointer-events-none absolute right-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
            </div>
          )}
          <button
            type="submit"
            disabled={!name.trim()}
            className="flex items-center gap-1.5 rounded-xl bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground transition-all hover:bg-primary/90 active:scale-95 disabled:opacity-40"
          >
            <Plus className="h-3.5 w-3.5" />
            Add
          </button>
          <button
            type="button"
            onClick={() => { setIsOpen(false); setName("") }}
            className="rounded-xl px-3 py-2.5 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted"
          >
            Done
          </button>
        </div>
      </div>
    </form>
  )
}
