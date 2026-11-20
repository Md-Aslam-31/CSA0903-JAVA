from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Preformatted, Table, TableStyle, PageBreak, Image as RLImage
from xml.sax.saxutils import escape

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'Assignment5'
font=ImageFont.truetype(r'C:\Windows\Fonts\consola.ttf',15)
small=ImageFont.truetype(r'C:\Windows\Fonts\consola.ttf',12)
files={k:A/f'Task{k}'/'outputs'/name for k,name in {
'B1':'pass-store-checks.txt','B2':'login-adversarial.txt','B3':'jdbc-benchmarks.txt','B4':'migration-metadata.txt'}.items()}
for task,p in files.items():
    lines=p.read_text(encoding='utf-8-sig').splitlines()
    if task=='B3':
        lines=[x for x in lines if not x.startswith(('DB product=','JDBC driver=','Tables=','PASS_ID ->','REG_NO ->','NAME ->','ROUTE_NO ->','AMOUNT ->'))]
        lines=['Task B3 - recorded JDBC run output']+lines
    else: lines=[f'Task {task} - recorded Java run output']+lines
    w=min(1500,max(1100,max(map(len,lines))*9+50));h=90+len(lines)*25
    im=Image.new('RGB',(w,h),(18,24,32));d=ImageDraw.Draw(im)
    d.rectangle((0,0,w,40),fill=(31,48,67));d.text((18,11),'OUTPUT TRANSCRIPT RENDER | from committed stdout file',font=small,fill=(180,209,235))
    for n,line in enumerate(lines):d.text((18,55+n*25),line,font=font,fill=(220,230,238))
    im.save(A/f'Task{task}'/'outputs'/f'{task.lower()}-output-transcript.png')

S=getSampleStyleSheet()
S.add(ParagraphStyle(name='Cover',parent=S['Title'],fontSize=23,leading=28,textColor=colors.HexColor('#16324f'),spaceAfter=12))
S.add(ParagraphStyle(name='H1x',parent=S['Heading1'],fontSize=16,leading=20,textColor=colors.HexColor('#16324f'),spaceBefore=8,spaceAfter=6))
S.add(ParagraphStyle(name='H2x',parent=S['Heading2'],fontSize=12,leading=15,textColor=colors.HexColor('#245b78'),spaceBefore=7,spaceAfter=4))
S.add(ParagraphStyle(name='Bodyx',parent=S['BodyText'],fontSize=9,leading=12,spaceAfter=5))
S.add(ParagraphStyle(name='Bulx',parent=S['BodyText'],fontSize=9,leading=12,leftIndent=11,firstLineIndent=-7,spaceAfter=3))
S.add(ParagraphStyle(name='CodeX',fontName='Courier',fontSize=7,leading=9,backColor=colors.HexColor('#f2f4f6'),borderColor=colors.HexColor('#d5dce2'),borderWidth=.4,borderPadding=4,spaceBefore=2,spaceAfter=5))
def tx(x):
    x=escape(x.strip()).replace('**','').replace('`','')
    return x.replace('→','&#8594;').replace('₹','&#8377;').replace('×','&#215;').replace('≥','&#8805;')
def table(lines):
    rows=[]
    for z in lines:
        cells=[c.strip() for c in z.strip().strip('|').split('|')]
        if all(set(c)<=set('-: ') for c in cells):continue
        rows.append([Paragraph(tx(c),S['Bodyx']) for c in cells])
    if not rows:return None
    n=max(map(len,rows)); t=Table(rows,colWidths=[(A4[0]-38*mm)/n]*n,repeatRows=1)
    t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#dceaf3')),('GRID',(0,0),(-1,-1),.35,colors.HexColor('#aab8c2')),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),4),('RIGHTPADDING',(0,0),(-1,-1),4),('TOPPADDING',(0,0),(-1,-1),3),('BOTTOMPADDING',(0,0),(-1,-1),3)]));return t
lines=(A/'REPORT.md').read_text(encoding='utf-8-sig').splitlines(); story=[]; i=0; code=False; buf=[]; first=True
while i<len(lines):
    z=lines[i]
    if z.startswith('```'):
        if not code:code=True;buf=[]
        else:story.append(Preformatted('\n'.join(buf),S['CodeX']));code=False
        i+=1;continue
    if code:buf.append(z);i+=1;continue
    if not z.strip():i+=1;continue
    if z.startswith('|'):
        q=[]
        while i<len(lines) and lines[i].startswith('|'):q.append(lines[i]);i+=1
        t=table(q)
        if t:story.extend([t,Spacer(1,4)])
        continue
    if z.startswith('# '):
        sty='Cover' if first else 'H1x';first=False;story.append(Paragraph(tx(z[2:]),S[sty]));i+=1;continue
    if z.startswith(('## ','### ')):
        story.append(Paragraph(tx(z.lstrip('# ')),S['H2x']));i+=1;continue
    if z.startswith('- '):story.append(Paragraph('&#8226; '+tx(z[2:]),S['Bulx']));i+=1;continue
    para=[z.strip()];i+=1
    while i<len(lines) and lines[i].strip() and not lines[i].startswith(('#','- ','```','|')):para.append(lines[i].strip());i+=1
    story.append(Paragraph(tx(' '.join(para)),S['Bodyx']))
story += [PageBreak(),Paragraph('Run output evidence',S['H1x']),Paragraph('Images below are readable renderings of committed stdout files, labeled as output transcript renders. They are not represented as screenshots of OnlineGDB. The PassStore example was run in OnlineGDB; benchmark/JDBC outputs were produced by local JDK 17 and H2 runs.',S['Bodyx'])]
for task in files:
    p=A/f'Task{task}'/'outputs'/f'{task.lower()}-output-transcript.png';im=Image.open(p)
    story += [Paragraph(f'Task {task}',S['H2x']),RLImage(str(p),width=170*mm,height=170*mm*im.height/im.width)]
    if task!='B4':story.append(PageBreak())
def footer(c,d):
    c.saveState();c.setStrokeColor(colors.HexColor('#d4dee6'));c.line(18*mm,15*mm,A4[0]-18*mm,15*mm);c.setFont('Helvetica',8);c.setFillColor(colors.HexColor('#53616d'));c.drawString(18*mm,10*mm,'Programming in Java | Assignment 5 | CO5');c.drawRightString(A4[0]-18*mm,10*mm,str(d.page));c.restoreState()
pdf=A/'Solution-Report.pdf';SimpleDocTemplate(str(pdf),pagesize=A4,rightMargin=19*mm,leftMargin=19*mm,topMargin=18*mm,bottomMargin=22*mm,title='Assignment 5 Solution Report',author='Mohamed').build(story,onFirstPage=footer,onLaterPages=footer);print(pdf)
