import gzip, io, struct, sys, os, json
from dataclasses import dataclass
from typing import Any

@dataclass
class Tag:
    t: int
    v: Any

NAMES={0:'END',1:'BYTE',2:'SHORT',3:'INT',4:'LONG',5:'FLOAT',6:'DOUBLE',7:'BYTE_ARRAY',8:'STRING',9:'LIST',10:'COMPOUND',11:'INT_ARRAY',12:'LONG_ARRAY'}

class Reader:
    def __init__(self,b): self.f=io.BytesIO(b)
    def read(self,n):
        b=self.f.read(n)
        if len(b)!=n: raise EOFError((n,len(b),self.f.tell()))
        return b
    def u8(self): return self.read(1)[0]
    def i8(self): return struct.unpack('>b',self.read(1))[0]
    def i16(self): return struct.unpack('>h',self.read(2))[0]
    def u16(self): return struct.unpack('>H',self.read(2))[0]
    def i32(self): return struct.unpack('>i',self.read(4))[0]
    def i64(self): return struct.unpack('>q',self.read(8))[0]
    def f32(self): return struct.unpack('>f',self.read(4))[0]
    def f64(self): return struct.unpack('>d',self.read(8))[0]
    def string(self):
        n=self.u16(); return self.read(n).decode('utf-8','surrogatepass')
    def payload(self,t):
        if t==0: return None
        if t==1: return self.i8()
        if t==2: return self.i16()
        if t==3: return self.i32()
        if t==4: return self.i64()
        if t==5: return self.f32()
        if t==6: return self.f64()
        if t==7:
            n=self.i32(); return self.read(n)
        if t==8: return self.string()
        if t==9:
            et=self.u8(); n=self.i32(); return (et,[Tag(et,self.payload(et)) for _ in range(n)])
        if t==10:
            out={}
            while True:
                nt=self.u8()
                if nt==0: break
                name=self.string(); out[name]=Tag(nt,self.payload(nt))
            return out
        if t==11:
            n=self.i32(); return [self.i32() for _ in range(n)]
        if t==12:
            n=self.i32(); return [self.i64() for _ in range(n)]
        raise ValueError(t)
    def root(self):
        t=self.u8(); name=self.string(); return name,Tag(t,self.payload(t))

class Writer:
    def __init__(self): self.f=io.BytesIO()
    def put(self,b): self.f.write(b)
    def u8(self,x): self.put(bytes([x&255]))
    def i8(self,x): self.put(struct.pack('>b',x))
    def i16(self,x): self.put(struct.pack('>h',x))
    def u16(self,x): self.put(struct.pack('>H',x))
    def i32(self,x): self.put(struct.pack('>i',x))
    def i64(self,x): self.put(struct.pack('>q',x))
    def f32(self,x): self.put(struct.pack('>f',x))
    def f64(self,x): self.put(struct.pack('>d',x))
    def string(self,s):
        b=s.encode('utf-8','surrogatepass'); self.u16(len(b)); self.put(b)
    def payload(self,tag):
        t,v=tag.t,tag.v
        if t==0: return
        if t==1: self.i8(v)
        elif t==2: self.i16(v)
        elif t==3: self.i32(v)
        elif t==4: self.i64(v)
        elif t==5: self.f32(v)
        elif t==6: self.f64(v)
        elif t==7: self.i32(len(v)); self.put(v)
        elif t==8: self.string(v)
        elif t==9:
            et,arr=v; self.u8(et); self.i32(len(arr));
            for x in arr: self.payload(x)
        elif t==10:
            for name,x in v.items():
                self.u8(x.t); self.string(name); self.payload(x)
            self.u8(0)
        elif t==11:
            self.i32(len(v)); [self.i32(x) for x in v]
        elif t==12:
            self.i32(len(v)); [self.i64(x) for x in v]
        else: raise ValueError(t)
    def root(self,name,tag):
        self.u8(tag.t); self.string(name); self.payload(tag); return self.f.getvalue()

def load(path):
    raw=open(path,'rb').read()
    if raw.startswith(b'\x1f\x8b'): raw=gzip.decompress(raw)
    return Reader(raw).root()

def save(path,name,tag,gz=True):
    raw=Writer().root(name,tag)
    if gz: raw=gzip.compress(raw,mtime=0)
    os.makedirs(os.path.dirname(path),exist_ok=True)
    open(path,'wb').write(raw)

def unwrap(tag):
    t,v=tag.t,tag.v
    if t==10: return {k:unwrap(x) for k,x in v.items()}
    if t==9: return [unwrap(x) for x in v[1]]
    if t==7: return list(v)
    return v

if __name__=='__main__':
    p=sys.argv[1]; name,root=load(p); print(name,root.t); print(json.dumps(unwrap(root),indent=2)[:20000])
