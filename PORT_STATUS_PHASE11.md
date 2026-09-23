# Archaion Forge 1.20.1 Port — Phase 11 Presentation Parity / Compile Preflight

## Starting point
Phase 11 is cumulative over the Phase 10 integration/pretest source. It does not
undo any Phase 1–10 gameplay, worldgen, Trial Spawner/Vault, item, network, or
AAA Particles work.

## Changes in Phase 11

### Exact thrown Echo Mace renderer restored
Phase 10 temporarily registered the thrown Echo Mace through vanilla
`ThrownItemRenderer`. The original Archaion renderer has now been reconstructed
for 1.20.1:
- renders the stored thrown mace stack in `FIXED` item context;
- 2x visual scale;
- interpolated yaw alignment minus 90 degrees;
- Z-axis spin of -30 degrees per tick;
- original shadow radius 0.15 and strength 0.75.

### Last of Deepslate falling-block renderer restored
The phase-3 terrain chunks were gameplay-functional but invisible in Phase 10.
They now render the synchronized carried `BlockState` exactly like the original
presentation logic:
- only model-render-shape blocks are drawn;
- 20 degrees/tick X-axis tumble;
- 0.5 shadow radius;
- block model rendered with the entity's packed light.

### Intentionally invisible helper entities corrected
A bytecode audit of the original 1.21.1 renderers confirmed that Echo Star,
Grimoray Spell, LOD Intercept Blast, and LOD Slam are *supposed* to have no
geometry renderer: their original renderers return `shouldRender=false`.
The shared 1.20.1 no-geometry renderer now does the same instead of merely
registering a renderer that happened to draw no model. This also avoids needless
render-dispatch work/name-tag paths for those helpers.

### Echo's Grace parity audit
No ammo-consumption change was made. The original 1.21.1 `EchosGraceItem`
constructs its Echo Charge projectile stacks internally and does not consume
Echo Charges from the player's inventory. Phase 10's free-fire behaviour is
therefore intentional parity, not a bug.

## Additional API preflight
Verified against Forge 1.20.1 API documentation before retaining the new client
code:
- `BlockRenderDispatcher.renderSingleBlock(BlockState, PoseStack, MultiBufferSource, int, int)` exists;
- `ItemRenderer.renderStatic(ItemStack, ItemDisplayContext, int, int, PoseStack, MultiBufferSource, Level, int)` exists;
- `EntityRenderer.shouldRender(T, Frustum, double, double, double)` exists.

The wider Phase 10 API audit also remains applicable: smithing-template
constructor, global loot-modifier registration, Ancient Keep jigsaw placement,
raw loot-table generation, packet distributor/channel APIs, damage type tags,
energy-swirl render type, Beacon renderer call, and block-entity render-bounds
hooks were all checked against 1.20.1 signatures.

## Cumulative static validation after Phase 11
- Active Java source files: 81.
- JSON/pack metadata files parsed: 158; parse errors: 0.
- Ancient Keep NBT templates checked: 20; structural read errors: 0.
- Active NeoForge references in Java/resources: 0.
- Active mixin configs: 0.
- No-classpath Java 17 parser/syntax pass: no syntax-level diagnostics found;
  missing Minecraft/Forge symbols remain expected without the real classpath.

## Still deliberately unfinished
- The five bespoke major-mob model/animation classes and their exact original
  animation runtime are still represented by the safe Phase 10 fallback model.
  That is the main remaining presentation port, not missing combat logic.
- Original Trial Spawner's rotating display-mob renderer is not yet reinstated.
  The custom 1.20.1 Trial Spawner gameplay itself remains implemented.
- Ambient Ancient Keep fog and custom boss-bar skin remain deferred until after
  first launch/profiling, as recorded in Phase 10.
- Real ForgeGradle compile/client launch/dedicated-server launch still remain the
  hard integration gate.

## Next best step
If the build environment remains unavailable, continue the client parity pass by
backporting the original Trial Spawner display renderer and then the bespoke mob
model/layer definitions. If a ForgeGradle-capable environment becomes available
first, compile immediately and prioritize actual compiler/runtime evidence over
further speculative edits.
