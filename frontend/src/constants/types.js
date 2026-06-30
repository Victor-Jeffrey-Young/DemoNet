export const TYPE_META = {
  game:      { label:'游戏', emoji:'🎮', icon:'game', heroColor:'from-emerald-950 via-emerald-900 to-gray-950', accent:'emerald', desc:'试玩版 & 浏览器可玩' },
  movie:     { label:'电影', emoji:'🎬', icon:'movie', heroColor:'from-red-950 via-neutral-950 to-stone-900', accent:'red', desc:'预告片 & 导演访谈' },
  anime:     { label:'动漫', emoji:'🎭', icon:'anime', heroColor:'from-violet-950 via-fuchsia-950 to-gray-950', accent:'violet', desc:'先导PV & 前5分钟试看' },
  boardgame: { label:'桌游', emoji:'🎲', icon:'boardgame', heroColor:'from-amber-950 via-yellow-950 to-gray-950', accent:'amber', desc:'规则教学 & 实况回放' },
  model:     { label:'模型', emoji:'🧩', icon:'model', heroColor:'from-slate-950 via-blue-950 to-gray-950', accent:'sky', desc:'开箱 & 360°展示' },
  book:      { label:'书籍', emoji:'📖', icon:'book', heroColor:'from-stone-100 via-amber-50 to-white', accent:'amber', desc:'试读 & 书评' },
  music:     { label:'音乐', emoji:'🎵', icon:'music', heroColor:'from-fuchsia-950 via-pink-950 to-gray-950', accent:'fuchsia', desc:'30s试听 & 测评' },
  digital:   { label:'数码', emoji:'📱', icon:'digital', heroColor:'from-cyan-950 via-slate-950 to-gray-950', accent:'cyan', desc:'开箱 & 评测' },
  coffee:    { label:'咖啡', emoji:'☕', icon:'coffee', heroColor:'from-amber-900 via-orange-950 to-gray-950', accent:'orange', desc:'风味 & 冲泡指南' },
  offline:   { label:'线下', emoji:'🏛️', icon:'offline', heroColor:'from-indigo-950 via-slate-950 to-gray-950', accent:'indigo', desc:'展览 & 活动' },
}

export const TYPE_LIST = Object.keys(TYPE_META)

export function getMeta(type) {
  const defaults = {
    game:      'bg-emerald-600',
    movie:     'bg-red-600',
    anime:     'bg-violet-600',
    boardgame: 'bg-amber-600',
    model:     'bg-sky-600',
    book:      'bg-amber-600',
    music:     'bg-fuchsia-600',
    digital:   'bg-cyan-600',
    coffee:    'bg-orange-600',
    offline:   'bg-indigo-600',
  }
  return {
    ...TYPE_META[type],
    pagBg: defaults[type] || 'bg-gray-600',
    ...(TYPE_META[type] ? {} : { label:type, emoji:'📦', heroColor:'from-gray-950 to-gray-900', accent:'gray', desc:'' })
  }
}
