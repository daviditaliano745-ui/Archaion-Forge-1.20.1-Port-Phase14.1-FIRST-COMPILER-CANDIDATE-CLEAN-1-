import json, os, shutil, sys
from pathlib import Path
sys.path.insert(0, '/mnt/data/archaion_port/phase8_work')
from nbt_tool import load, save, Tag

ROOT = Path('/mnt/data/archaion_port/phase8_work/Archaion-Forge-1.20.1-BigBatch3')
SRC = ROOT / 'port-notes/original-data-1.21.1/data'
OUT = ROOT / 'src/main/resources/data'
if OUT.exists(): shutil.rmtree(OUT)
OUT.mkdir(parents=True)

def jread(p): return json.loads(Path(p).read_text(encoding='utf-8'))
def jwrite(p,obj):
    p=Path(p); p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(json.dumps(obj,indent=2,ensure_ascii=False)+"\n",encoding='utf-8')

def cpjson(src,dst, transform=None):
    o=jread(src)
    if transform: o=transform(o)
    jwrite(dst,o)

# ---- Worldgen: backport the Keep to a custom 1.20.1 jigsaw structure type ----
orig_struct=jread(SRC/'archaion/worldgen/structure/ancient_keep.json')
# 1.20.1 has no dimension_padding/liquid_settings and vanilla JigsawStructure caps size at 7.
# Keep Archaion's custom type; AncientKeepStructure reuses vanilla jigsaw placement with a size-20 codec.
keep={k:v for k,v in orig_struct.items() if k not in ('dimension_padding','liquid_settings')}
keep['type']='archaion:ancient_keep'
# 1.20.1 has no 'encapsulate' terrain adjustment. beard_box is the closest available behavior.
keep['terrain_adaptation']='beard_box'
# Trial Chambers tag does not exist in vanilla 1.20.1; keep broadly in overworld.
keep['biomes']='#minecraft:is_overworld'
# 1.20.1 heightmap codec name.
if keep.get('project_start_to_heightmap')=='WORLD_SURFACE': keep['project_start_to_heightmap']='WORLD_SURFACE_WG'
jwrite(OUT/'archaion/worldgen/structure/ancient_keep.json',keep)

orig_set=jread(SRC/'archaion/worldgen/structure_set/ancient_keep.json')
placement=orig_set['placement']
new_set={
    'structures':orig_set['structures'],
    'placement':{
        'type':'minecraft:random_spread',
        'salt':placement['salt'],
        'spacing':placement['spacing'],
        'separation':placement['separation'],
        'spread_type':placement.get('spread_type','linear')
    }
}
jwrite(OUT/'archaion/worldgen/structure_set/ancient_keep.json',new_set)

# Processor list: custom 1.21 vault processor only stripped obsolete vault data.
proc=jread(SRC/'archaion/worldgen/processor_list/ancient_keep/ancient_keep_decay.json')
proc['processors']=[p for p in proc['processors'] if p.get('processor_type')!='archaion:deepslate_vault']
jwrite(OUT/'archaion/worldgen/processor_list/ancient_keep/ancient_keep_decay.json',proc)

# Pools: copy but remove references to the two zero-byte templates shipped in upstream.
pool_src=SRC/'archaion/worldgen/template_pool/ancient_keep'
for f in pool_src.glob('*.json'):
    o=jread(f)
    o['elements']=[e for e in o.get('elements',[]) if e.get('element',{}).get('location') not in (
        'archaion:ancient_keep/misc_room','archaion:ancient_keep/misc_room_x')]
    jwrite(OUT/'archaion/worldgen/template_pool/ancient_keep'/f.name,o)

# Structure tag used by exploration maps.
cpjson(SRC/'archaion/tags/worldgen/structure/on_ancient_keep_maps.json',
       OUT/'archaion/tags/worldgen/structure/on_ancient_keep_maps.json')

# ---- Structure templates: migrate names/folders and prune 1.21 BE/component data ----
struct_src=SRC/'archaion/structure/ancient_keep'
struct_out=OUT/'archaion/structures/ancient_keep'

def clean_compound(comp):
    # 1.21 item/block-entity components are not present in 1.20.1.
    comp.pop('components',None)
    # New jigsaw priority fields are harmless but 1.20.1 does not know them.
    comp.pop('placement_priority',None)
    comp.pop('selection_priority',None)
    idtag=comp.get('id')
    if idtag and idtag.t==8 and idtag.v=='minecraft:vault':
        idtag.v='archaion:trial_vault'
        for k in ('server_data','shared_data','config'): comp.pop(k,None)
    # recursively clean nested compounds/lists
    for t in list(comp.values()): clean_tag(t)

def clean_tag(tag):
    if tag.t==10: clean_compound(tag.v)
    elif tag.t==9:
        _et,arr=tag.v
        for x in arr: clean_tag(x)

converted_templates=[]
for f in sorted(struct_src.glob('*.nbt')):
    if f.stat().st_size==0:
        continue
    name,root=load(f)
    if root.t!=10: raise ValueError(f'not compound root: {f}')
    root.v['DataVersion']=Tag(3,3465)
    clean_compound(root.v)
    dst=struct_out/f.name
    save(dst,name,root,True)
    converted_templates.append(f.name)

# ---- Recipes: old 1.20.1 result syntax and optional mace compatibility tag ----
rec_src=SRC/'archaion/recipe'
for f in rec_src.glob('*.json'):
    o=jread(f)
    result=o.get('result')
    if isinstance(result,dict) and 'id' in result:
        result['item']=result.pop('id')
    if f.name=='echo_mace_upgrade_smithing.json':
        # Vanilla 1.20.1 has no mace. Define a mod-extensible optional tag instead of hard failing datapack load.
        o['base']={'tag':'forge:maces'}
    jwrite(OUT/'archaion/recipes'/f.name,o)
jwrite(OUT/'forge/tags/items/maces.json',{
    'replace':False,
    'values':[{'id':'minecraft:mace','required':False}]
})

# ---- Loot tables: carry compatible gameplay loot; omit 1.21-only runtime data handled in Java ----
def clean_loot(o, density=False):
    o.pop('random_sequence',None)
    def walk(x):
        if isinstance(x,dict):
            x.pop('target',None) if x.get('function')=='minecraft:set_name' else None
            # 1.21's explicit add=false is accepted by newer codecs but unnecessary; remove where safe.
            for k,v in list(x.items()): walk(v)
        elif isinstance(x,list):
            for v in x: walk(v)
    walk(o)
    if density:
        for pool in o.get('pools',[]):
            ents=pool.get('entries',[])
            pool['entries']=[e for e in ents if 'minecraft:density' not in json.dumps(e)]
    return o

for rel in ['blocks/deepslate_pillar.json','blocks/soul_lamp.json','entities/brave.json','entities/grimoray.json','entities/haunter.json','entities/last_of_deepslate.json','chests/ancient_keep_map.json']:
    o=jread(SRC/'archaion/loot_table'/rel)
    o=clean_loot(o,density=(rel=='entities/grimoray.json'))
    jwrite(OUT/'archaion/loot_tables'/rel,o)

# ---- Forge global loot modifiers: 40% Ancient Keep map chance in Ancient City chests ----
# Split the two source tables into separate modifiers. This is equivalent to the original OR condition
# and avoids relying on cross-version composite-condition codec names.
glm_entries=[]
for suffix, table in (
    ('ancient_city', 'minecraft:chests/ancient_city'),
    ('ancient_city_ice_box', 'minecraft:chests/ancient_city_ice_box'),
):
    name=f'ancient_keep_map_in_{suffix}'
    glm_entries.append(f'archaion:{name}')
    jwrite(OUT/'archaion/loot_modifiers'/f'{name}.json',{
        'type':'archaion:add_loot_table',
        'conditions':[
            {'condition':'forge:loot_table_id','loot_table_id':table},
            {'condition':'minecraft:random_chance','chance':0.4}
        ],
        'loot_table':'archaion:chests/ancient_keep_map'
    })
jwrite(OUT/'forge/loot_modifiers/global_loot_modifiers.json',{
    'replace':False,
    'entries':glm_entries
})

# ---- Core block/item tags that exist in 1.20.1 ----
for rel in ['block/walls.json','block/mineable/pickaxe.json','item/walls.json','item/breaks_decorated_pots.json']:
    s=SRC/'minecraft/tags'/rel
    if s.exists():
        # 1.20.1 tag registry folder names are plural.
        parts=rel.split('/')
        kind={'block':'blocks','item':'items'}[parts[0]]
        dst=OUT/'minecraft/tags'/kind/Path(*parts[1:])
        cpjson(s,dst)

# ---- Advancements: port 1.21 item predicate/icon field shapes to 1.20.1 ----
adv_src=SRC/'archaion/advancement'
for f in adv_src.glob('*.json'):
    o=jread(f)
    o.pop('sends_telemetry_event',None)
    disp=o.get('display',{})
    icon=disp.get('icon')
    if isinstance(icon,dict) and 'id' in icon:
        icon['item']=icon.pop('id'); icon.pop('count',None)
    # 1.20.1 ItemPredicate encodes item ids as an `items` list; 1.21 allows a singular string.
    def advwalk(x):
        if isinstance(x,dict):
            if 'items' in x and isinstance(x['items'],str):
                x['items']=[x['items']]
            for v in list(x.values()): advwalk(v)
        elif isinstance(x,list):
            for v in x: advwalk(v)
    advwalk(o)
    # 1.20.1 location predicates use singular `structure`. Keep the player-condition shape
    # used by vanilla's 1.20.1 structure-location advancements.
    if f.name=='enter_ancient_keep.json':
        crit=next(iter(o['criteria'].values()))
        crit['conditions']={'player':[
            {
                'condition':'minecraft:entity_properties',
                'entity':'this',
                'predicate':{'location':{'structure':'archaion:ancient_keep'}}
            }
        ]}
    jwrite(OUT/'archaion/advancements'/f.name,o)

# ---- Summary for validation/docs ----
summary={
    'converted_structure_templates':len(converted_templates),
    'skipped_zero_byte_templates':['misc_room.nbt','misc_room_x.nbt'],
    'data_version':3465,
    'worldgen_structure_type':'archaion:ancient_keep (custom 1.20.1 jigsaw wrapper; depth 15 preserved)',
    'structure_placement':'minecraft:random_spread',
}
Path('/mnt/data/archaion_port/phase8_work/phase8_conversion_summary.json').write_text(json.dumps(summary,indent=2)+'\n')
print(json.dumps(summary,indent=2))
