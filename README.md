# Shulker on Back

Open the shulker box (or ender chest) you carry in the **Curios back slot** with a single key.

Minecraft 1.20.1 / Forge. MIT.

## What it does

- Press the key (default **B**) to open whatever storage you are wearing on your back
- **Shulker boxes** open a normal shulker screen; the contents live in the item's NBT
- **Ender chests** open your personal ender inventory
- Anything else on your back is ignored (a short message tells you why)

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
