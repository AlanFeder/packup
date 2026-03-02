"use client"

import { useState, useRef } from "react"
import Image from "next/image"
import { Sunrise, Camera } from "lucide-react"
import { cn } from "@/lib/utils"
import type { FamilyMember, PackingItem } from "@/lib/packing-data"

interface MemberSelectorProps {
  members: FamilyMember[]
  activeMemberId: string
  morningItems: PackingItem[]
  onSelect: (id: string) => void
  onUpdatePhoto: (memberId: string, photoUrl: string) => void
}

export function MemberSelector({
  members,
  activeMemberId,
  morningItems,
  onSelect,
  onUpdatePhoto,
}: MemberSelectorProps) {
  const isMorning = activeMemberId === "morning"
  const [editingPhotoFor, setEditingPhotoFor] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  // Sort: members who are NOT fully done first, fully done members at the end
  const sortedMembers = [...members].sort((a, b) => {
    const aTotal = a.items.length
    const aDone = a.items.filter((i) => i.status === "done").length
    const aSnoozed = a.items.filter((i) => i.status === "snoozed").length
    const aAllDone = (aDone + aSnoozed) === aTotal && aTotal > 0

    const bTotal = b.items.length
    const bDone = b.items.filter((i) => i.status === "done").length
    const bSnoozed = b.items.filter((i) => i.status === "snoozed").length
    const bAllDone = (bDone + bSnoozed) === bTotal && bTotal > 0

    if (aAllDone && !bAllDone) return 1
    if (!aAllDone && bAllDone) return -1
    return 0
  })

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file || !editingPhotoFor) return
    const url = URL.createObjectURL(file)
    onUpdatePhoto(editingPhotoFor, url)
    setEditingPhotoFor(null)
    e.target.value = ""
  }

  const handlePhotoLongPress = (memberId: string) => {
    setEditingPhotoFor(memberId)
    setTimeout(() => fileInputRef.current?.click(), 50)
  }

  return (
    <nav aria-label="Family members" className="px-4">
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={handleFileChange}
      />
      <div className="flex gap-3 overflow-x-auto pb-1 scrollbar-none">
        {sortedMembers.map((member) => {
          const isActive = member.id === activeMemberId
          const doneCount = member.items.filter((i) => i.status === "done").length
          const snoozedCount = member.items.filter((i) => i.status === "snoozed").length
          const totalCount = member.items.length
          const handledCount = doneCount + snoozedCount
          const allDone = handledCount === totalCount && totalCount > 0

          return (
            <button
              key={member.id}
              onClick={() => onSelect(member.id)}
              onContextMenu={(e) => {
                e.preventDefault()
                handlePhotoLongPress(member.id)
              }}
              className={cn(
                "group relative flex shrink-0 flex-col items-center gap-1.5 rounded-2xl px-2 py-2 transition-all duration-200",
                isActive ? "bg-primary/10" : "hover:bg-muted/60",
                allDone && !isActive && "opacity-60"
              )}
              aria-pressed={isActive}
              aria-label={`${member.name}'s packing list, ${handledCount} of ${totalCount} handled`}
            >
              {/* Avatar with ring */}
              <div
                className={cn(
                  "relative h-12 w-12 rounded-full p-[2px] transition-all duration-300",
                  isActive
                    ? "ring-2 ring-primary ring-offset-2 ring-offset-background"
                    : allDone
                      ? "ring-2 ring-success/40 ring-offset-1 ring-offset-background"
                      : ""
                )}
              >
                <Image
                  src={member.photo}
                  alt={member.name}
                  width={48}
                  height={48}
                  className="h-full w-full rounded-full object-cover"
                  unoptimized={member.photo.startsWith("blob:")}
                />
                {/* Progress ring overlay */}
                {totalCount > 0 && !allDone && (
                  <svg
                    className="pointer-events-none absolute inset-0 -rotate-90"
                    viewBox="0 0 48 48"
                    aria-hidden="true"
                  >
                    <circle
                      cx="24" cy="24" r="22"
                      fill="none"
                      stroke="var(--outline-variant)"
                      strokeWidth="2"
                      opacity="0.3"
                    />
                    <circle
                      cx="24" cy="24" r="22"
                      fill="none"
                      stroke={isActive ? "var(--primary)" : "var(--success)"}
                      strokeWidth="2.5"
                      strokeLinecap="round"
                      strokeDasharray={`${2 * Math.PI * 22}`}
                      strokeDashoffset={`${2 * Math.PI * 22 * (1 - handledCount / totalCount)}`}
                      className="transition-all duration-700"
                    />
                  </svg>
                )}
                {allDone && (
                  <div className="absolute -bottom-0.5 -right-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-success text-success-foreground md3-elevation-1">
                    <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
                      <path d="M2 5.5L4 7.5L8 3" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  </div>
                )}
                {/* Camera overlay on hover for photo change */}
                <div
                  onClick={(e) => {
                    e.stopPropagation()
                    handlePhotoLongPress(member.id)
                  }}
                  className="absolute inset-0 flex items-center justify-center rounded-full bg-foreground/0 opacity-0 transition-all group-hover:bg-foreground/30 group-hover:opacity-100"
                >
                  <Camera className="h-4 w-4 text-card" />
                </div>
              </div>
              {/* Name */}
              <span
                className={cn(
                  "max-w-[56px] truncate text-[11px] font-medium leading-tight transition-colors",
                  isActive ? "text-primary" : "text-muted-foreground"
                )}
              >
                {member.name}
              </span>
            </button>
          )
        })}

        {/* Morning tab */}
        <button
          onClick={() => onSelect("morning")}
          className={cn(
            "group relative flex shrink-0 flex-col items-center gap-1.5 rounded-2xl px-2 py-2 transition-all duration-200",
            isMorning ? "bg-warning/10" : "hover:bg-muted/60"
          )}
          aria-pressed={isMorning}
          aria-label={`Morning of travel list, ${morningItems.length} items`}
        >
          <div
            className={cn(
              "flex h-12 w-12 items-center justify-center rounded-full transition-all duration-200",
              isMorning
                ? "bg-warning/20 ring-2 ring-warning ring-offset-2 ring-offset-background"
                : "bg-warning/10"
            )}
          >
            <Sunrise
              className={cn(
                "h-5 w-5 transition-colors",
                isMorning ? "text-warning-foreground" : "text-warning-foreground/60"
              )}
            />
          </div>
          <span
            className={cn(
              "max-w-[56px] truncate text-[11px] font-medium leading-tight transition-colors",
              isMorning ? "text-warning-foreground" : "text-muted-foreground"
            )}
          >
            Morning
          </span>
          {morningItems.length > 0 && (
            <span className="absolute right-0.5 top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-warning px-1 text-[9px] font-bold text-warning-foreground">
              {morningItems.length}
            </span>
          )}
        </button>
      </div>
    </nav>
  )
}
