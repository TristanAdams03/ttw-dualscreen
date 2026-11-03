# TTW DualScreen protocol v0

JSON text frames over WebSocket, one object per frame. Every message has a `"type"`.
Example frames for each message are in `fixtures/`.

| Dir | Type | Fields | Fixture |
|-----|------|--------|---------|
| C->S | `hello` | `client:string`, `protocol:0` | hello.json |
| S->C | `welcome` | `plugin:"0.0.1-spike"`, `protocol:0`, `game:string`, `xnvse:string`, `ttw:bool`, `features:["spike"]` | welcome.json |
| C->S | `subscribe` | `topics:["spike"]`, `rateHz:int 1..10` | subscribe.json |
| S->C | `state` | `topic:"spike"`, `data:{...}` (below) | state_spike.json |
| C->S | `command` | `id:string`, `name:"spike.equipFirstWeapon"\|"spike.dumpWorld"`, `args:{}` | command_equip.json |
| S->C | `result` | `id:string`, `ok:bool`, `error:string\|null`, `data:object\|null` | result_ok.json, result_no_save.json |
| C->S | `ping` | `t:int` | ping.json |
| S->C | `pong` | `t:int` | pong.json |
| S->C | `error` | `message:string` | error.json |

## state `spike` data

`frame:int`, `ms:int`, `saveLoaded:bool`, `hp:float|null`, `hpMax:float|null`, `hardcore:bool|null`,
`bInDCWasteland:int|null`, `bInNVWasteland:int|null`, `worldspace:string|null`, `cell:string|null`,
`interior:bool|null`, `colors:{hudMain,hudAlt,terminal,pipboy,menu}|null` where each color is `[r,g,b]`.

## Rules

- `pong` is answered by the network thread, so it proves the socket works even if the game's main loop stalls.
- `error` is sent for unparseable or unknown messages; the connection stays open.
