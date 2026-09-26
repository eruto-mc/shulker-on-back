# Shulker on Back

Open the shulker box (or ender chest) you carry in the **Curios back slot** with a single key,
and decide with an item tag what may go on the back at all.

Minecraft 1.20.1 / Forge. MIT.

## What it does

- Press the key (default **B**) to open whatever storage you are wearing on your back
- **Shulker boxes** open a normal shulker screen; the contents live in the item's NBT
- **Ender chests** open your personal ender inventory
- Anything else on your back is ignored (a short message tells you why)
- The item tag **`curios:back_weapon`** decides what may be worn on the back

## The back slot filter

Back Weapon Slot answers `CurioEquipEvent` with `Result.ALLOW`, and
`DynamicStackHandler.isItemValid` returns `true` the moment it sees `ALLOW`. The tag check
never runs, so **any item at all** can be dragged onto the back. Its
`disallow_curio_items` option does not fix this: it only rejects items that own a Curios
slot of their own, so dirt and torches still go on.

`BackSlotFilter` subscribes to the same event at `EventPriority.LOWEST`, which runs after
Back Weapon Slot, and replaces the result with a plain tag test:

```java
event.setResult(event.getStack().is(ALLOWED) ? Result.ALLOW : Result.DENY);
```

The other way in — this mod's own B-key swap — asks `CuriosApi.isStackValid` instead, which
never fires the event. That path reads the slot's `validators`, and Back Weapon Slot ships
none, so `testCurioPredicates` on an empty set returns `false` and **nothing** could be
swapped on. Add `"validators": ["curios:tag"]` to `back_weapon` in a data pack and both ways
in are decided by the same tag. Curios merges slot definitions field by field, so a data pack
that writes only `validators` keeps the original `order` and `icon`.

**If the tag is empty the filter stands down.** Data packs are server-side only, so a missing
one would otherwise lock the slot completely; instead the mod logs one warning and leaves the
result alone.

## Requirements

- [Curios API](https://modrinth.com/mod/curios)
- A mod that provides the `back_weapon` slot — for example
  [Back Weapon Slot](https://modrinth.com/mod/back-weapon-slot)

Without them the key simply does nothing.

## Design notes

- **The contents are written back to the item on every change**, not on close.
  Closing-only saves lose everything if the server stops or the player dies while the
  screen is open.
- **Vanilla `ShulkerBoxMenu` is reused** rather than a custom menu. Two reasons:
  its `ShulkerBoxSlot` already refuses to put a shulker box inside a shulker box,
  and `MenuType.SHULKER_BOX` already has a client screen, so no client-side
  registration is needed.
- **The screen closes if the item leaves the back slot** (death, commands, another mod).
  Rare, but editing a detached stack would silently drop items.
- **The screen reloads if something else rewrote the box while it is open** (1.2).
  Forge's shulker item handler writes the box contents when another mod goes through it —
  for example Spoiled replacing a rotted food item. Before 1.2 the screen kept its own copy
  and the next click wrote that copy back: the rotten item turned back into food, and anything
  added through the handler in the meantime was lost. The screen now remembers the `Items`
  list it last read or wrote and reloads when that list has been replaced (checked every tick
  before syncing, and before every click). It compares by identity, not contents, because
  Spoiled edits the spoil timer in place.
- An empty shulker box has its `BlockEntityTag` removed so it stacks again, like vanilla.

## Verified

Automated, with a scripted client (key press and real container clicks through
`handleInventoryMouseClick`, so server-side slot rules apply):

| Check | |
| - | - |
| Key is registered | OK |
| Nothing happens when the back slot is empty | OK |
| Shulker box opens (`ShulkerBoxMenu`, 63 slots) | OK |
| Item can be picked up and placed (positive control) | OK |
| **Contents written to the item NBT while the screen is open** | OK |
| Shulker box cannot be placed inside (stays in hand) | OK |
| Screen closes when the item leaves the back slot | OK |
| Ender chest opens (`ChestMenu`) | OK |
