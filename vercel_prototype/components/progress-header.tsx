"use client"

import Image from "next/image"
import { CheckCircle2 } from "lucide-react"
import type { FamilyMember } from "@/lib/packing-data"

interface ProgressHeaderProps {
  member: FamilyMember
}

export function ProgressHeader({ member }: ProgressHeaderProps) {
  const total = member.items.length
  const done = member.items.filter((i) => i.status === "done").length
  const snoozed = member.items.filter((i) => i.status === "snoozed").length
  const remaining = total - done - snoozed
  const progress = total > 0 ? ((done + snoozed) / total) * 100 : 0
  const allDone = remaining === 0 && total > 0

  return (
    <div className="flex items-center gap-4">
      {/* Circular progress with photo */}
      <div className="relative h-16 w-16 shrink-0">
        <svg className="h-full w-full -rotate-90" viewBox="0 0 64 64" aria-hidden="true">
          <circle
            cx="32" cy="32" r="28"
            fill="none"
            stroke="var(--outline-variant)"
            strokeWidth="3"
            opacity="0.3"
          />
          <circle
            cx="32" cy="32" r="28"
            fill="none"
            stroke={allDone ? "var(--success)" : "var(--primary)"}
            strokeWidth="3.5"
            strokeLinecap="round"
            strokeDasharray={`${2 * Math.PI * 28}`}
            strokeDashoffset={`${2 * Math.PI * 28 * (1 - progress / 100)}`}
            className="transition-all duration-700 ease-out"
          />
        </svg>
        <div className="absolute inset-[5px] overflow-hidden rounded-full">
          <Image
            src={member.photo}
            alt={member.name}
            width={54}
            height={54}
            className="h-full w-full object-cover"
          />
        </div>
        {allDone && (
          <div className="absolute -bottom-1 -right-1 flex h-6 w-6 items-center justify-center rounded-full bg-success text-success-foreground md3-elevation-1">
            <CheckCircle2 className="h-3.5 w-3.5" />
          </div>
        )}
      </div>

      {/* Text */}
      <div className="flex-1">
        <h2 className="text-lg font-semibold tracking-tight text-foreground text-balance">
          {allDone ? "All packed!" : `${member.name}`}
        </h2>
        <p className="mt-0.5 text-sm text-muted-foreground">
          {allDone
            ? "Everything is packed or snoozed to morning"
            : `${done} packed${snoozed > 0 ? ` \u00B7 ${snoozed} morning` : ""} \u00B7 ${remaining} left`}
        </p>
        {/* Linear progress bar */}
        {!allDone && (
          <div className="mt-2 h-1 w-full overflow-hidden rounded-full bg-muted">
            <div
              className="h-full rounded-full bg-primary transition-all duration-500"
              style={{ width: `${progress}%` }}
            />
          </div>
        )}
      </div>
    </div>
  )
}
