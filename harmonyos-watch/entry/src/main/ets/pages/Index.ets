import { common } from '@kit.AbilityKit';
import {
  WATCH_GAMES,
  WatchClassroomItem,
  WatchEnemyItem,
  WatchGame,
  WatchSocialLink
} from '../model/WatchModels';
import {
  loadWatchClassroom,
  loadWatchEnemies,
  loadWatchSocialLinks
} from '../data/WatchDataLoader';

interface HubParams {
  game: WatchGame;
}

interface SlDetailParams {
  game: WatchGame;
  link: WatchSocialLink;
}

@Entry
@Component
struct Index {
  @Provide('pathStack') pathStack: NavPathStack = new NavPathStack();

  @Builder
  PageMap(name: string, param: Object) {
    if (name === 'hub') {
      HubPage({ game: (param as HubParams).game })
    } else if (name === 'classroom') {
      ClassroomPage({ game: (param as HubParams).game })
    } else if (name === 'enemies') {
      EnemiesPage({ game: (param as HubParams).game })
    } else if (name === 'sociallinks') {
      SocialLinksPage({ game: (param as HubParams).game })
    } else if (name === 'sldetail') {
      SocialLinkDetailPage({
        game: (param as SlDetailParams).game,
        link: (param as SlDetailParams).link
      })
    }
  }

  build() {
    Navigation(this.pathStack) {
      List({ space: 8 }) {
        ListItem() {
          Column() {
            Text('PERSONA')
              .fontSize(15)
              .fontWeight(FontWeight.Bold)
              .fontColor('#ECECF1')
              .letterSpacing(1.5)
            Text('WATCH COMPANION')
              .fontSize(10)
              .fontWeight(FontWeight.Medium)
              .fontColor('#C9A45C')
              .letterSpacing(1.2)
              .margin({ top: 2 })
          }
          .width('100%')
          .padding({ top: 18, bottom: 6 })
          .alignItems(HorizontalAlign.Center)
        }

        ForEach(WATCH_GAMES, (game: WatchGame) => {
          ListItem() {
            Row() {
              Row()
                .width(4)
                .height(24)
                .borderRadius(2)
                .backgroundColor(game.accentColor)
                .margin({ right: 10 })

              Column() {
                Text(game.shortTitle)
                  .fontSize(15)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#ECECF1')
                Text(game.title)
                  .fontSize(11)
                  .fontColor('#9C9CAD')
                  .maxLines(1)
              }
              .layoutWeight(1)
              .alignItems(HorizontalAlign.Start)

              Text('›')
                .fontSize(18)
                .fontColor('#9C9CAD')
            }
            .width('100%')
            .padding({ left: 12, right: 14, top: 10, bottom: 10 })
            .borderRadius(14)
            .backgroundColor('#1B1B24')
            .border({ width: 1, color: '#262632' })
            .onClick(() => {
              const p: HubParams = { game: game };
              this.pathStack.pushPath({ name: 'hub', param: p });
            })
          }
        }, (game: WatchGame) => game.id)
      }
      .width('100%')
      .height('100%')
      .padding({ left: 16, right: 16, bottom: 26 })
      .backgroundColor('#0D0D12')
      .scrollBar(BarState.Off)
    }
    .mode(NavigationMode.Stack)
    .hideTitleBar(true)
    .navDestination(this.PageMap)
    .backgroundColor('#0D0D12')
  }
}

@Component
struct HubPage {
  @Consume('pathStack') pathStack: NavPathStack;
  @Prop game: WatchGame;

  build() {
    NavDestination() {
      List({ space: 8 }) {
        ListItem() {
          Column() {
            Text(this.game.shortTitle)
              .fontSize(15)
              .fontWeight(FontWeight.Bold)
              .fontColor(this.game.accentColor)
            Text(this.game.title)
              .fontSize(11)
              .fontColor('#9C9CAD')
          }
          .width('100%')
          .padding({ top: 18, bottom: 6 })
          .alignItems(HorizontalAlign.Center)
        }

        ListItem() {
          this.MenuRow('Classroom Answers', 'Dates & exams', 'classroom')
        }
        ListItem() {
          this.MenuRow('Enemy Weaknesses', 'Affinities & bosses', 'enemies')
        }
        ListItem() {
          this.MenuRow(
            this.game.seriesId === 'p5' ? 'Confidants' : 'Social Links',
            'Best hangout answers',
            'sociallinks'
          )
        }
      }
      .width('100%')
      .height('100%')
      .padding({ left: 16, right: 16, bottom: 26 })
      .backgroundColor('#0D0D12')
    }
    .hideTitleBar(true)
    .backgroundColor('#0D0D12')
  }

  @Builder
  MenuRow(title: string, sub: string, route: string) {
    Row() {
      Column() {
        Text(title)
          .fontSize(14)
          .fontWeight(FontWeight.Bold)
          .fontColor('#ECECF1')
        Text(sub)
          .fontSize(11)
          .fontColor('#9C9CAD')
      }
      .layoutWeight(1)
      .alignItems(HorizontalAlign.Start)

      Text('›')
        .fontSize(18)
        .fontColor('#9C9CAD')
    }
    .width('100%')
    .padding({ left: 14, right: 14, top: 12, bottom: 12 })
    .borderRadius(14)
    .backgroundColor('#1B1B24')
    .border({ width: 1, color: '#262632' })
    .onClick(() => {
      const p: HubParams = { game: this.game };
      this.pathStack.pushPath({ name: route, param: p });
    })
  }
}

@Component
struct ClassroomPage {
  @Prop game: WatchGame;
  @State items: WatchClassroomItem[] = [];
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    const ctx = getContext(this) as common.Context;
    loadWatchClassroom(ctx, this.game.id).then((res: WatchClassroomItem[]) => {
      this.items = res;
      this.isLoading = false;
    });
  }

  build() {
    NavDestination() {
      if (this.isLoading) {
        Column() {
          LoadingProgress().width(32).height(32).color(this.game.accentColor)
        }
        .width('100%')
        .height('100%')
        .justifyContent(FlexAlign.Center)
        .backgroundColor('#0D0D12')
      } else {
        List({ space: 8 }) {
          ListItem() {
            Text(`${this.game.shortTitle} Classroom`)
              .fontSize(14)
              .fontWeight(FontWeight.Bold)
              .fontColor(this.game.accentColor)
              .width('100%')
              .textAlign(TextAlign.Center)
              .padding({ top: 18, bottom: 4 })
          }

          ForEach(this.items, (item: WatchClassroomItem) => {
            ListItem() {
              Column() {
                Row() {
                  Text(item.date)
                    .fontSize(11)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(this.game.accentColor)
                  if (item.isExam) {
                    Text('EXAM')
                      .fontSize(9)
                      .fontWeight(FontWeight.Bold)
                      .fontColor('#FFB74D')
                      .margin({ left: 6 })
                  }
                }
                Text(item.answer)
                  .fontSize(14)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#ECECF1')
                  .margin({ top: 3 })
                Text(item.question)
                  .fontSize(11)
                  .fontColor('#9C9CAD')
                  .margin({ top: 2 })
              }
              .width('100%')
              .padding(12)
              .borderRadius(12)
              .backgroundColor('#1B1B24')
              .border({ width: 1, color: '#262632' })
              .alignItems(HorizontalAlign.Start)
            }
          }, (item: WatchClassroomItem) => item.id)
        }
        .width('100%')
        .height('100%')
        .padding({ left: 16, right: 16, bottom: 28 })
        .backgroundColor('#0D0D12')
      }
    }
    .hideTitleBar(true)
    .backgroundColor('#0D0D12')
  }
}

@Component
struct EnemiesPage {
  @Prop game: WatchGame;
  @State allEnemies: WatchEnemyItem[] = [];
  @State query: string = '';
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    const ctx = getContext(this) as common.Context;
    loadWatchEnemies(ctx, this.game.id).then((res: WatchEnemyItem[]) => {
      this.allEnemies = res;
      this.isLoading = false;
    });
  }

  private getFiltered(): WatchEnemyItem[] {
    const q = this.query.trim().toLowerCase();
    if (q.length === 0) {
      return this.allEnemies.slice(0, 80);
    }
    const matched: WatchEnemyItem[] = [];
    for (let i = 0; i < this.allEnemies.length; i++) {
      const e = this.allEnemies[i];
      if (e.name.toLowerCase().includes(q) || e.arcana.toLowerCase().includes(q)) {
        matched.push(e);
        if (matched.length >= 60) {
          break;
        }
      }
    }
    return matched;
  }

  build() {
    NavDestination() {
      if (this.isLoading) {
        Column() {
          LoadingProgress().width(32).height(32).color(this.game.accentColor)
        }
        .width('100%')
        .height('100%')
        .justifyContent(FlexAlign.Center)
        .backgroundColor('#0D0D12')
      } else {
        List({ space: 8 }) {
          ListItem() {
            Text(`${this.game.shortTitle} Shadows`)
              .fontSize(14)
              .fontWeight(FontWeight.Bold)
              .fontColor(this.game.accentColor)
              .width('100%')
              .textAlign(TextAlign.Center)
              .padding({ top: 16 })
          }

          ListItem() {
            TextInput({ placeholder: 'Filter shadow...', text: this.query })
              .height(36)
              .fontSize(12)
              .fontColor('#ECECF1')
              .placeholderColor('#9C9CAD')
              .backgroundColor('#14141B')
              .borderRadius(10)
              .onChange((val: string) => {
                this.query = val;
              })
          }

          ForEach(this.getFiltered(), (enemy: WatchEnemyItem) => {
            ListItem() {
              Column() {
                Row() {
                  Text(enemy.name)
                    .fontSize(14)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#ECECF1')
                    .layoutWeight(1)
                  Text(`Lv.${enemy.level}`)
                    .fontSize(11)
                    .fontColor('#C9A45C')
                }
                .width('100%')

                if (enemy.weaknesses.length > 0) {
                  Text(`Weak: ${enemy.weaknesses.join(', ')}`)
                    .fontSize(12)
                    .fontWeight(FontWeight.Medium)
                    .fontColor('#EF5350')
                    .margin({ top: 4 })
                } else {
                  Text('No elemental weaknesses')
                    .fontSize(11)
                    .fontColor('#9C9CAD')
                    .margin({ top: 4 })
                }
              }
              .width('100%')
              .padding(12)
              .borderRadius(12)
              .backgroundColor('#1B1B24')
              .border({ width: 1, color: '#262632' })
              .alignItems(HorizontalAlign.Start)
            }
          }, (enemy: WatchEnemyItem) => enemy.id)
        }
        .width('100%')
        .height('100%')
        .padding({ left: 16, right: 16, bottom: 28 })
        .backgroundColor('#0D0D12')
      }
    }
    .hideTitleBar(true)
    .backgroundColor('#0D0D12')
  }
}

@Component
struct SocialLinksPage {
  @Consume('pathStack') pathStack: NavPathStack;
  @Prop game: WatchGame;
  @State links: WatchSocialLink[] = [];
  @State pinnedIds: string[] = [];
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    const ctx = getContext(this) as common.Context;
    loadWatchSocialLinks(ctx, this.game.id).then((res: WatchSocialLink[]) => {
      this.links = res;
      this.isLoading = false;
    });
  }

  private isPinned(id: string): boolean {
    return this.pinnedIds.indexOf(id) !== -1;
  }

  private togglePin(id: string): void {
    const idx = this.pinnedIds.indexOf(id);
    if (idx >= 0) {
      const next = this.pinnedIds.slice();
      next.splice(idx, 1);
      this.pinnedIds = next;
    } else {
      const next = this.pinnedIds.slice();
      next.push(id);
      this.pinnedIds = next;
    }
  }

  private getOrderedLinks(): WatchSocialLink[] {
    const pinned: WatchSocialLink[] = [];
    const rest: WatchSocialLink[] = [];
    for (let i = 0; i < this.links.length; i++) {
      if (this.isPinned(this.links[i].id)) {
        pinned.push(this.links[i]);
      } else {
        rest.push(this.links[i]);
      }
    }
    return pinned.concat(rest);
  }

  build() {
    NavDestination() {
      if (this.isLoading) {
        Column() {
          LoadingProgress().width(32).height(32).color(this.game.accentColor)
        }
        .width('100%')
        .height('100%')
        .justifyContent(FlexAlign.Center)
        .backgroundColor('#0D0D12')
      } else {
        List({ space: 8 }) {
          ListItem() {
            Text(this.game.seriesId === 'p5' ? 'Confidants' : 'Social Links')
              .fontSize(14)
              .fontWeight(FontWeight.Bold)
              .fontColor(this.game.accentColor)
              .width('100%')
              .textAlign(TextAlign.Center)
              .padding({ top: 18, bottom: 4 })
          }

          ForEach(this.getOrderedLinks(), (link: WatchSocialLink) => {
            ListItem() {
              Row() {
                Column() {
                  Text(link.arcana)
                    .fontSize(14)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#ECECF1')
                  Text(`${link.ranks.length} Ranks`)
                    .fontSize(11)
                    .fontColor('#9C9CAD')
                }
                .layoutWeight(1)
                .alignItems(HorizontalAlign.Start)

                Text(this.isPinned(link.id) ? 'PINNED' : 'PIN')
                  .fontSize(10)
                  .fontWeight(FontWeight.Bold)
                  .fontColor(this.isPinned(link.id) ? '#C9A45C' : '#9C9CAD')
                  .padding({ left: 8, right: 8, top: 4, bottom: 4 })
                  .borderRadius(6)
                  .backgroundColor('#14141B')
                  .onClick(() => {
                    this.togglePin(link.id);
                  })
              }
              .width('100%')
              .padding(12)
              .borderRadius(12)
              .backgroundColor('#1B1B24')
              .border({
                width: 1,
                color: this.isPinned(link.id) ? '#C9A45C' : '#262632'
              })
              .onClick(() => {
                const p: SlDetailParams = { game: this.game, link: link };
                this.pathStack.pushPath({ name: 'sldetail', param: p });
              })
            }
          }, (link: WatchSocialLink) => `${link.id}_${this.isPinned(link.id)}`)
        }
        .width('100%')
        .height('100%')
        .padding({ left: 16, right: 16, bottom: 28 })
        .backgroundColor('#0D0D12')
      }
    }
    .hideTitleBar(true)
    .backgroundColor('#0D0D12')
  }
}

@Component
struct SocialLinkDetailPage {
  @Prop game: WatchGame;
  @Prop link: WatchSocialLink;

  build() {
    NavDestination() {
      List({ space: 8 }) {
        ListItem() {
          Text(this.link.arcana)
            .fontSize(15)
            .fontWeight(FontWeight.Bold)
            .fontColor(this.game.accentColor)
            .width('100%')
            .textAlign(TextAlign.Center)
            .padding({ top: 18, bottom: 4 })
        }

        ForEach(this.link.ranks, (r: WatchSocialLinkRank) => {
          ListItem() {
            Column() {
              Text(r.rankName)
                .fontSize(13)
                .fontWeight(FontWeight.Bold)
                .fontColor('#C9A45C')
              ForEach(r.dialogues, (d: WatchDialogue) => {
                Column() {
                  if (d.question.length > 0) {
                    Text(d.question)
                      .fontSize(11)
                      .fontColor('#9C9CAD')
                      .margin({ top: 6 })
                  }
                  ForEach(d.choices, (c: WatchDialogueChoice) => {
                    Text(`+${c.points}: ${c.text}`)
                      .fontSize(12)
                      .fontColor('#ECECF1')
                      .margin({ top: 2 })
                  })
                }
                .alignItems(HorizontalAlign.Start)
              })
            }
            .width('100%')
            .padding(12)
            .borderRadius(12)
            .backgroundColor('#1B1B24')
            .border({ width: 1, color: '#262632' })
            .alignItems(HorizontalAlign.Start)
          }
        })
      }
      .width('100%')
      .height('100%')
      .padding({ left: 16, right: 16, bottom: 28 })
      .backgroundColor('#0D0D12')
    }
    .hideTitleBar(true)
    .backgroundColor('#0D0D12')
  }
}
