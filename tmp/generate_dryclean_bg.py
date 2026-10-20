#!/usr/bin/env python3
import subprocess

svg_content = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1080 1920" width="1080" height="1920">
  <defs>
    <!-- Background atmospheric gradients -->
    <linearGradient id="bgGrad" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#040D1A"/>
      <stop offset="25%" stop-color="#081E38"/>
      <stop offset="60%" stop-color="#0D2C50"/>
      <stop offset="100%" stop-color="#050F1E"/>
    </linearGradient>

    <!-- Warm boutique spotlight -->
    <radialGradient id="spotLight1" cx="30%" cy="18%" r="45%">
      <stop offset="0%" stop-color="#FFE8B5" stop-opacity="0.35"/>
      <stop offset="50%" stop-color="#60A5FA" stop-opacity="0.15"/>
      <stop offset="100%" stop-color="#040D1A" stop-opacity="0"/>
    </radialGradient>

    <radialGradient id="spotLight2" cx="75%" cy="22%" r="50%">
      <stop offset="0%" stop-color="#93C5FD" stop-opacity="0.30"/>
      <stop offset="60%" stop-color="#1E3A8A" stop-opacity="0.10"/>
      <stop offset="100%" stop-color="#040D1A" stop-opacity="0"/>
    </radialGradient>

    <!-- Metallic conveyor rail gradient -->
    <linearGradient id="chromeRail" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#64748B"/>
      <stop offset="30%" stop-color="#E2E8F0"/>
      <stop offset="50%" stop-color="#FFFFFF"/>
      <stop offset="70%" stop-color="#CBD5E1"/>
      <stop offset="100%" stop-color="#475569"/>
    </linearGradient>

    <!-- Translucent plastic garment bag sheen -->
    <linearGradient id="bagSheen" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.32"/>
      <stop offset="35%" stop-color="#BAE6FD" stop-opacity="0.12"/>
      <stop offset="70%" stop-color="#FFFFFF" stop-opacity="0.22"/>
      <stop offset="100%" stop-color="#38BDF8" stop-opacity="0.06"/>
    </linearGradient>

    <linearGradient id="suitNavy" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#0F172A"/>
      <stop offset="40%" stop-color="#1E293B"/>
      <stop offset="70%" stop-color="#273549"/>
      <stop offset="100%" stop-color="#0F172A"/>
    </linearGradient>

    <linearGradient id="shirtWhite" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#E2E8F0"/>
      <stop offset="50%" stop-color="#FFFFFF"/>
      <stop offset="100%" stop-color="#CBD5E1"/>
    </linearGradient>

    <linearGradient id="dressBurgundy" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#4C0519"/>
      <stop offset="50%" stop-color="#881337"/>
      <stop offset="100%" stop-color="#4C0519"/>
    </linearGradient>

    <linearGradient id="coatCamel" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#78350F"/>
      <stop offset="50%" stop-color="#B45309"/>
      <stop offset="100%" stop-color="#78350F"/>
    </linearGradient>

    <linearGradient id="blouseSky" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#0284C7"/>
      <stop offset="50%" stop-color="#38BDF8"/>
      <stop offset="100%" stop-color="#0369A1"/>
    </linearGradient>
  </defs>

  <!-- Background Base -->
  <rect width="1080" height="1920" fill="url(#bgGrad)"/>
  
  <!-- Ambient Atmospheric Spotlights -->
  <rect width="1080" height="1920" fill="url(#spotLight1)"/>
  <rect width="1080" height="1920" fill="url(#spotLight2)"/>

  <!-- Background Architectural Grid / Luxury Boutique Wall Panels -->
  <g opacity="0.12" stroke="#BAE6FD" stroke-width="1.5">
    <line x1="80" y1="0" x2="80" y2="1920"/>
    <line x1="280" y1="0" x2="280" y2="1920"/>
    <line x1="480" y1="0" x2="480" y2="1920"/>
    <line x1="680" y1="0" x2="680" y2="1920"/>
    <line x1="880" y1="0" x2="880" y2="1920"/>
    <line x1="1000" y1="0" x2="1000" y2="1920"/>

    <line x1="0" y1="240" x2="1080" y2="240"/>
    <line x1="0" y1="520" x2="1080" y2="520"/>
    <line x1="0" y1="880" x2="1080" y2="880"/>
    <line x1="0" y1="1300" x2="1080" y2="1300"/>
    <line x1="0" y1="1680" x2="1080" y2="1680"/>
  </g>

  <!-- High-end Commercial Dry Cleaning Conveyor Rail (Top Track) -->
  <g id="conveyor-rail">
    <!-- Ceiling Mount Brackets -->
    <rect x="180" y="0" width="16" height="130" fill="#334155"/>
    <rect x="532" y="0" width="16" height="130" fill="#334155"/>
    <rect x="884" y="0" width="16" height="130" fill="#334155"/>
    
    <!-- Heavy Chrome Oval Carousel Rail -->
    <path d="M-50,130 Q540,155 1130,130" fill="none" stroke="url(#chromeRail)" stroke-width="14" stroke-linecap="round"/>
    <path d="M-50,146 Q540,171 1130,146" fill="none" stroke="#1E293B" stroke-width="6"/>

    <!-- Conveyor numbered links / slots -->
    <circle cx="90" cy="133" r="5" fill="#E2E8F0"/>
    <circle cx="210" cy="138" r="5" fill="#E2E8F0"/>
    <circle cx="340" cy="142" r="5" fill="#E2E8F0"/>
    <circle cx="470" cy="144" r="5" fill="#E2E8F0"/>
    <circle cx="610" cy="144" r="5" fill="#E2E8F0"/>
    <circle cx="740" cy="142" r="5" fill="#E2E8F0"/>
    <circle cx="870" cy="138" r="5" fill="#E2E8F0"/>
    <circle cx="990" cy="133" r="5" fill="#E2E8F0"/>
  </g>

  <!-- GARMENTS ROW (Freshly Cleaned Clothes in Protective Dry-Cleaning Bags) -->
  
  <!-- GARMENT 1: Left - Winter Coat / Jacket in protective bag -->
  <g id="garment-1" transform="translate(60, 134)">
    <!-- Hanger Hook & Wooden Hanger -->
    <path d="M30,0 C30,-22 45,-26 50,-15 C55,-5 42,10 30,14" fill="none" stroke="#CBD5E1" stroke-width="4"/>
    <path d="M-60,34 L30,14 L120,34" fill="none" stroke="#B45309" stroke-width="8" stroke-linecap="round"/>
    <!-- Outer Plastic Garment Cover / Dry Cleaning Bag -->
    <polygon points="-75,34 30,12 135,34 145,740 -85,740" fill="url(#bagSheen)" stroke="#FFFFFF" stroke-opacity="0.35" stroke-width="2"/>
    <!-- Suit Silhouette inside -->
    <path d="M-55,42 L-40,240 L-65,680 L125,680 L100,240 L115,42 Z" fill="url(#coatCamel)" opacity="0.85"/>
    <polygon points="10,42 30,140 50,42" fill="#FEF3C7"/>
    <!-- Dry Clean Service Ticket Tag -->
    <rect x="70" y="80" width="38" height="60" rx="3" fill="#FFFFFF" stroke="#0061E0" stroke-width="1.5"/>
    <text x="74" y="96" font-family="sans-serif" font-size="8" font-weight="bold" fill="#0061E0">SNOW</text>
    <text x="74" y="112" font-family="monospace" font-size="7" fill="#334155">#8812</text>
    <text x="74" y="126" font-family="sans-serif" font-size="6" fill="#16A34A">CLEAN</text>
    <!-- Plastic fold sheen line -->
    <line x1="-30" y1="50" x2="-5" y2="720" stroke="#FFFFFF" stroke-opacity="0.3" stroke-width="3"/>
    <line x1="85" y1="50" x2="65" y2="720" stroke="#BAE6FD" stroke-opacity="0.25" stroke-width="2"/>
  </g>

  <!-- GARMENT 2: Left-Center - Crisp White Executive Shirts in Bag -->
  <g id="garment-2" transform="translate(250, 138)">
    <!-- Hanger Hook & Wire Hanger -->
    <path d="M40,0 C40,-20 54,-24 58,-14 C62,-4 50,8 40,12" fill="none" stroke="#E2E8F0" stroke-width="3.5"/>
    <path d="M-50,30 L40,12 L130,30" fill="none" stroke="#E2E8F0" stroke-width="5" stroke-linecap="round"/>
    <!-- Plastic Garment Cover -->
    <polygon points="-65,30 40,10 145,30 152,680 -72,680" fill="url(#bagSheen)" stroke="#FFFFFF" stroke-opacity="0.4" stroke-width="2"/>
    <!-- Crisp White Shirt Inside -->
    <path d="M-45,36 L-35,220 L-50,620 L130,620 L115,220 L125,36 Z" fill="url(#shirtWhite)" opacity="0.95"/>
    <!-- Stiff Pressed Collar & Silk Tie -->
    <polygon points="20,34 40,78 60,34" fill="#0061E0"/>
    <polygon points="34,65 40,240 46,65" fill="#0047BA"/>
    <!-- White Shirt Pressed Placket with buttons -->
    <line x1="40" y1="78" x2="40" y2="600" stroke="#CBD5E1" stroke-width="3"/>
    <circle cx="40" cy="140" r="3.5" fill="#94A3B8"/>
    <circle cx="40" cy="220" r="3.5" fill="#94A3B8"/>
    <circle cx="40" cy="300" r="3.5" fill="#94A3B8"/>
    <circle cx="40" cy="380" r="3.5" fill="#94A3B8"/>
    <!-- Plastic bag hanger opening & sheen -->
    <line x1="10" y1="40" x2="-20" y2="660" stroke="#FFFFFF" stroke-opacity="0.4" stroke-width="4"/>
    <line x1="105" y1="40" x2="80" y2="660" stroke="#E0F2FE" stroke-opacity="0.3" stroke-width="2"/>
    <!-- Yellow Dry Clean Tag -->
    <rect x="-40" y="70" width="34" height="52" rx="2" fill="#FEF08A" stroke="#CA8A04" stroke-width="1"/>
    <text x="-36" y="85" font-family="sans-serif" font-size="7" font-weight="bold" fill="#854D0E">EXPRESS</text>
    <text x="-36" y="98" font-family="monospace" font-size="6" fill="#713F12">#4190</text>
  </g>

  <!-- GARMENT 3: Center Stage - Masterpiece Tailored Two-Piece Navy Suit -->
  <g id="garment-3" transform="translate(460, 142)">
    <!-- Polished Chrome Hanger -->
    <path d="M50,0 C50,-24 66,-28 72,-16 C78,-4 64,12 50,16" fill="none" stroke="#FFFFFF" stroke-width="4"/>
    <path d="M-60,38 L50,16 L160,38" fill="none" stroke="#334155" stroke-width="10" stroke-linecap="round"/>
    <!-- Transparent Protective Garment Bag with Zipper -->
    <polygon points="-80,36 50,12 180,36 190,820 -90,820" fill="url(#bagSheen)" stroke="#FFFFFF" stroke-opacity="0.5" stroke-width="2.5"/>
    <line x1="50" y1="18" x2="50" y2="820" stroke="#BAE6FD" stroke-opacity="0.6" stroke-width="2" stroke-dasharray="6,4"/>
    <!-- Luxury Navy Tuxedo / Suit Silhouette -->
    <path d="M-60,42 L-45,280 L-70,760 L170,760 L145,280 L160,42 Z" fill="url(#suitNavy)" opacity="0.95"/>
    <!-- Silk Satin Lapels (Shawl Collar) -->
    <path d="M5,42 L40,320 L50,335 L60,320 L95,42" fill="none" stroke="#38BDF8" stroke-width="12" stroke-opacity="0.8"/>
    <!-- Crisp White Dress Shirt & Bowtie beneath -->
    <polygon points="35,42 50,110 65,42" fill="#FFFFFF"/>
    <!-- Bow tie in Snowhite Blue -->
    <polygon points="36,60 50,70 36,80" fill="#0061E0"/>
    <polygon points="64,60 50,70 64,80" fill="#0061E0"/>
    <circle cx="50" cy="70" r="4" fill="#0047BA"/>
    <!-- Breast Pocket with White Silk Pocket Square -->
    <rect x="-15" y="160" width="38" height="6" fill="#1E293B"/>
    <polygon points="-5,160 3,142 12,160" fill="#FFFFFF"/>
    <!-- Shiny Suite Buttons -->
    <circle cx="50" cy="380" r="5" fill="#0284C7"/>
    <circle cx="50" cy="460" r="5" fill="#0284C7"/>
    <!-- Translucent Sheen Highlights on Plastic Cover -->
    <line x1="-35" y1="45" x2="-60" y2="780" stroke="#FFFFFF" stroke-opacity="0.45" stroke-width="5"/>
    <line x1="130" y1="45" x2="100" y2="780" stroke="#BAE6FD" stroke-opacity="0.35" stroke-width="3"/>
    <!-- Snowhite VIP Tag -->
    <rect x="110" y="90" width="46" height="70" rx="4" fill="#0061E0" stroke="#FFFFFF" stroke-width="1.5"/>
    <text x="116" y="110" font-family="sans-serif" font-size="8" font-weight="bold" fill="#FFFFFF">SW VIP</text>
    <text x="116" y="126" font-family="monospace" font-size="7" fill="#BAE6FD">PASSED</text>
    <text x="116" y="142" font-family="sans-serif" font-size="7" font-weight="bold" fill="#4ADE80">100%</text>
  </g>

  <!-- GARMENT 4: Right-Center - Evening Gown / Silk Dress in Bag -->
  <g id="garment-4" transform="translate(680, 140)">
    <!-- Hanger Hook & Elegant Curved Hanger -->
    <path d="M45,0 C45,-22 60,-26 64,-15 C68,-4 56,10 45,14" fill="none" stroke="#CBD5E1" stroke-width="3.5"/>
    <path d="M-45,32 L45,14 L135,32" fill="none" stroke="#CBD5E1" stroke-width="6" stroke-linecap="round"/>
    <!-- Transparent Gown Bag (Extra Long) -->
    <polygon points="-65,32 45,10 155,32 170,860 -75,860" fill="url(#bagSheen)" stroke="#FFFFFF" stroke-opacity="0.4" stroke-width="2"/>
    <!-- Deep Burgundy / Wine Red Evening Gown -->
    <path d="M-35,38 L-25,180 L-5,320 L-65,820 L155,820 L95,320 L115,180 L125,38 Z" fill="url(#dressBurgundy)" opacity="0.9"/>
    <!-- Neckline & Shimmer details -->
    <path d="M15,40 Q45,95 75,40" fill="none" stroke="#FDA4AF" stroke-width="3"/>
    <!-- Sparkles on dress -->
    <circle cx="25" cy="240" r="2.5" fill="#FECDD3"/>
    <circle cx="55" cy="380" r="2" fill="#FECDD3"/>
    <circle cx="10" cy="520" r="3" fill="#FECDD3"/>
    <circle cx="80" cy="640" r="2.5" fill="#FECDD3"/>
    <!-- Plastic Gleam reflection -->
    <line x1="-15" y1="42" x2="-45" y2="820" stroke="#FFFFFF" stroke-opacity="0.38" stroke-width="3.5"/>
    <line x1="105" y1="42" x2="80" y2="820" stroke="#E0F2FE" stroke-opacity="0.28" stroke-width="2"/>
    <!-- Dry Clean Tag -->
    <rect x="-42" y="75" width="34" height="52" rx="2" fill="#FFFFFF" stroke="#0061E0" stroke-width="1"/>
    <text x="-38" y="90" font-family="sans-serif" font-size="7" font-weight="bold" fill="#0061E0">SILK</text>
    <text x="-38" y="104" font-family="monospace" font-size="6" fill="#475569">#2914</text>
  </g>

  <!-- GARMENT 5: Far Right - Silk Blouse & Blazer in Bag -->
  <g id="garment-5" transform="translate(880, 134)">
    <path d="M35,0 C35,-20 50,-24 54,-14 C58,-4 46,8 35,12" fill="none" stroke="#94A3B8" stroke-width="3"/>
    <path d="M-55,32 L35,12 L125,32" fill="none" stroke="#64748B" stroke-width="6" stroke-linecap="round"/>
    <polygon points="-70,32 35,10 140,32 150,700 -80,700" fill="url(#bagSheen)" stroke="#FFFFFF" stroke-opacity="0.3" stroke-width="1.8"/>
    <path d="M-50,38 L-35,210 L-55,640 L125,640 L105,210 L115,38 Z" fill="url(#blouseSky)" opacity="0.85"/>
    <line x1="-20" y1="45" x2="-40" y2="670" stroke="#FFFFFF" stroke-opacity="0.3" stroke-width="3"/>
  </g>

  <!-- Subtle Floating Sparkle Particles / Spotless Clean Effects in Center -->
  <g id="sparkle-particles" opacity="0.75">
    <!-- Star Sparkle 1 -->
    <path d="M 280,480 Q 280,510 310,510 Q 280,510 280,540 Q 280,510 250,510 Q 280,510 280,480 Z" fill="#FFFFFF"/>
    <!-- Star Sparkle 2 -->
    <path d="M 820,380 Q 820,405 845,405 Q 820,405 820,430 Q 820,405 795,405 Q 820,405 820,380 Z" fill="#BAE6FD"/>
    <!-- Star Sparkle 3 -->
    <path d="M 540,680 Q 540,700 560,700 Q 540,700 540,720 Q 540,700 520,700 Q 540,700 540,680 Z" fill="#FFFFFF"/>
    <!-- Star Sparkle 4 -->
    <path d="M 160,780 Q 160,800 180,800 Q 160,800 160,820 Q 160,800 140,800 Q 160,800 160,780 Z" fill="#93C5FD"/>
    <!-- Star Sparkle 5 -->
    <path d="M 920,860 Q 920,880 940,880 Q 920,880 920,900 Q 920,880 900,880 Q 920,880 920,860 Z" fill="#FFFFFF"/>

    <!-- Subtle soft bokeh circles -->
    <circle cx="210" cy="620" r="32" fill="#BAE6FD" opacity="0.15"/>
    <circle cx="860" cy="580" r="44" fill="#93C5FD" opacity="0.12"/>
    <circle cx="420" cy="820" r="26" fill="#FFFFFF" opacity="0.14"/>
    <circle cx="680" cy="740" r="38" fill="#60A5FA" opacity="0.12"/>
  </g>

  <!-- Lower Boutique Counter / Finished Pressing Table Glow -->
  <linearGradient id="counterGlow" x1="0%" y1="0%" x2="0%" y2="100%">
    <stop offset="0%" stop-color="#0284C7" stop-opacity="0"/>
    <stop offset="50%" stop-color="#0061E0" stop-opacity="0.15"/>
    <stop offset="100%" stop-color="#040D1A" stop-opacity="0.8"/>
  </linearGradient>
  <rect x="0" y="1100" width="1080" height="820" fill="url(#counterGlow)"/>

  <!-- Crisp Stacked Linens / Towels at bottom sides -->
  <g id="bottom-linens" opacity="0.35">
    <rect x="60" y="1520" width="220" height="28" rx="10" fill="#E2E8F0"/>
    <rect x="50" y="1556" width="240" height="32" rx="12" fill="#CBD5E1"/>
    <rect x="40" y="1596" width="260" height="36" rx="14" fill="#94A3B8"/>

    <rect x="800" y="1530" width="220" height="28" rx="10" fill="#E2E8F0"/>
    <rect x="790" y="1566" width="240" height="32" rx="12" fill="#BAE6FD"/>
    <rect x="780" y="1606" width="260" height="36" rx="14" fill="#60A5FA"/>
  </g>

</svg>"""

with open("/tmp/dryclean_bg.svg", "w") as f:
    f.write(svg_content)

# Convert SVG to PNG using ImageMagick
cmd = [
    "convert",
    "-background", "none",
    "-size", "1080x1920",
    "/tmp/dryclean_bg.svg",
    "PNG32:/app/src/main/res/drawable/img_dryclean_bg.png"
]
result = subprocess.run(cmd, capture_output=True, text=True)
print("Conversion result code:", result.returncode)
if result.stderr:
    print("stderr:", result.stderr)
