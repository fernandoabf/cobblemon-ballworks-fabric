Textures for Ballworks items.

SHIPPED (all 16x16 RGBA):
  ball_anvil.png                     — inventory icon for the anvil block
  base_blank.png                     — flat iron-gray stamped disc
  pokeball_base.png                  — shaded lower hemisphere, iron-gray
  <ball>_lid_blank.png  (x31)        — flat stamped disc in the ball's top color
  <ball>_lid.png        (x31)        — shaded upper hemisphere in the ball's top color
  <tier>_hammer.png     (x18)        — tiered hammer sprites, imported from the
                                        Pixelmon hammer bundle
  lids/<ball>balllid.png (x23)       — legacy Pixelmon-style lid PNGs (staged for the BER)

The <ball>_lid_blank and <ball>_lid sprites were procedurally generated from the
Poke Ball sprite sheets in poke_balls/: each ball's dominant top-hemisphere color
was extracted and used to fill a flat disc (the "blank") and a shaded hemisphere
(the "lid"). Four shades per sprite plus a near-black outline.

Ball short ids covered by the lid_blank/lid set:
  azure, beast, citrine, dive, dream, dusk, fast, friend, great, heal,
  heavy, level, love, lure, luxury, master, moon, nest, net, park, poke,
  premier, quick, repeat, roseate, safari, slate, sport, timer, ultra,
  verdant
